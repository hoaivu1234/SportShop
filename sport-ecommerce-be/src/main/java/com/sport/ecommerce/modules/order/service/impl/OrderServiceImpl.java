package com.sport.ecommerce.modules.order.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.cart.entity.CartItem;
import com.sport.ecommerce.modules.cart.repository.CartItemRepository;
import com.sport.ecommerce.modules.cart.repository.CartRepository;
import com.sport.ecommerce.modules.order.dto.request.CheckoutRequest;
import com.sport.ecommerce.modules.order.dto.request.OrderStatusUpdateRequest;
import com.sport.ecommerce.modules.order.dto.response.OrderResponse;
import com.sport.ecommerce.modules.order.dto.response.OrderSummaryResponse;
import com.sport.ecommerce.modules.order.entity.Order;
import com.sport.ecommerce.modules.order.entity.OrderItem;
import com.sport.ecommerce.modules.order.enums.OrderStatus;
import com.sport.ecommerce.modules.order.mapper.OrderMapper;
import com.sport.ecommerce.modules.order.producer.OrderEmailProducer;
import com.sport.ecommerce.modules.order.repository.OrderRepository;
import com.sport.ecommerce.modules.order.service.OrderService;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import com.sport.ecommerce.modules.product.repository.ProductImageRepository;
import com.sport.ecommerce.modules.product.repository.ProductVariantRepository;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository          orderRepository;
    private final CartRepository           cartRepository;
    private final CartItemRepository       cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository   productImageRepository;
    private final UserRepository           userRepository;
    private final OrderMapper              orderMapper;
    private final OrderEmailProducer       orderEmailProducer;

    // ── Place order ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse placeOrder(CheckoutRequest request) {
        User user = getCurrentUser();

        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST.value(), "Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCartIdWithVariant(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Cart is empty");
        }

        // ── Batch-fetch main images to avoid N+1 ─────────────────────────────
        List<Long> productIds = cartItems.stream()
                .map(ci -> ci.getProductVariant().getProduct().getId())
                .distinct()
                .toList();
        Map<Long, String> mainImages = productImageRepository
                .findMainImagesByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImageUrl,
                        (first, second) -> first
                ));

        // ── Build order aggregate ─────────────────────────────────────────────
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .shippingAddress(orderMapper.toEntity(request.getShippingAddress()))
                .notes(request.getNotes())
                .paymentStatus(request.getPaymentMethod() != null
                        ? request.getPaymentMethod() : "COD")
                .shippingFee(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            // Acquire pessimistic lock per variant to prevent overselling
            ProductVariant variant = variantRepository
                    .findByIdWithLock(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND.value(),
                            "Variant not found: " + cartItem.getProductVariant().getId()));

            int available = variant.getStock() != null ? variant.getStock() : 0;
            if (cartItem.getQuantity() > available) {
                throw new BusinessException(HttpStatus.CONFLICT.value(),
                        "Insufficient stock for \"" + variant.getProduct().getName()
                                + "\" (" + variant.getSku() + ")"
                                + ". Available: " + available
                                + ", requested: " + cartItem.getQuantity());
            }

            // Deduct stock
            variant.setStock(available - cartItem.getQuantity());

            // Unit price: prefer the locked-in cart snapshot, fall back to current price
            BigDecimal unitPrice = cartItem.getPriceSnapshot() != null
                    ? cartItem.getPriceSnapshot()
                    : variant.getPrice();
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            String imageUrl = mainImages.get(variant.getProduct().getId());

            OrderItem item = OrderItem.builder()
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .variantSku(variant.getSku())
                    .variantSize(variant.getSize())
                    .variantColor(variant.getColor())
                    .productImageUrl(imageUrl)
                    .price(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .subtotal(lineSubtotal)
                    .build();

            order.addItem(item);
            subtotal = subtotal.add(lineSubtotal);
        }

        order.setSubtotal(subtotal);
        order.setTotalPrice(subtotal
                .subtract(order.getDiscountAmount())
                .add(order.getShippingFee()));

        Order saved = orderRepository.save(order);

        // ── Clear the cart ────────────────────────────────────────────────────
        cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("Order {} placed by user {} ({} items, total={})",
                saved.getOrderNumber(), user.getId(), cartItems.size(), saved.getTotalPrice());

        orderEmailProducer.sendOrderSuccessEmail(order);

        return toDetailResponse(saved);
    }

    // ── Customer queries ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(Pageable pageable) {
        User user = getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(orderMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        User user = getCurrentUser();
        Order order = findWithItems(id);
        assertOwnerOrAdmin(order, user);
        return toDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        User user = getCurrentUser();
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(), "Order not found: " + orderNumber));
        assertOwnerOrAdmin(order, user);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        User user = getCurrentUser();
        Order order = findWithItems(id);
        assertOwnerOrAdmin(order, user);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Only PENDING orders can be cancelled by the customer");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);
        return toDetailResponse(orderRepository.save(order));
    }

    // ── Admin queries ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(String status, Pageable pageable) {
        OrderStatus parsed = parseStatus(status);
        return orderRepository.findAllByStatus(parsed, pageable)
                .map(orderMapper::toSummary);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = findWithItems(id);
        validateTransition(order.getStatus(), request.getStatus());
        order.setStatus(request.getStatus());

        if (request.getStatus() == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        return toDetailResponse(orderRepository.save(order));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Order findWithItems(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(), "Order not found: " + id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED.value(), "Authenticated user not found"));
    }

    private void assertOwnerOrAdmin(Order order, User user) {
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(),
                    "Access denied to order " + order.getId());
        }
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.SHIPPED   || next == OrderStatus.CANCELLED;
            case SHIPPED    -> next == OrderStatus.COMPLETED;
            default         -> false;
        };
        if (!valid) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProductVariant() == null) continue;
            variantRepository.findById(item.getProductVariant().getId()).ifPresent(v -> {
                int restored = (v.getStock() != null ? v.getStock() : 0) + item.getQuantity();
                v.setStock(restored);
                variantRepository.save(v);
                log.debug("Restored {} units to variant {}", item.getQuantity(), v.getId());
            });
        }
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Unknown order status: " + status);
        }
    }

    private OrderResponse toDetailResponse(Order order) {
        return orderMapper.toResponse(order);
    }

    private static final DateTimeFormatter ORDER_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String generateOrderNumber() {
        String ts  = LocalDateTime.now().format(ORDER_TS);
        int    rnd = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD-" + ts + "-" + rnd;
    }
}
