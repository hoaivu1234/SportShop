package com.sport.ecommerce.modules.user.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.order.entity.Order;
import com.sport.ecommerce.modules.order.repository.OrderRepository;
import com.sport.ecommerce.modules.user.dto.response.CustomerDetailResponse;
import com.sport.ecommerce.modules.user.dto.response.CustomerSummaryResponse;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import com.sport.ecommerce.modules.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryResponse> getCustomers(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size);
        String dbStatus = resolveStatusFilter(status);
        Page<User> users = userRepository.searchCustomers(keyword, dbStatus, pageable);
        Page<CustomerSummaryResponse> result = users.map(user -> {
            long orderCount = orderRepository.countByUserId(user.getId());
            BigDecimal ltv   = orderRepository.sumTotalPriceByUserId(user.getId());
            return toSummary(user, orderCount, ltv);
        });
        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Customer not found with id: " + id));

        long orderCount          = orderRepository.countByUserId(id);
        BigDecimal ltv           = orderRepository.sumTotalPriceByUserId(id);
        List<Order> recentOrders = orderRepository.findTop3ByUserIdOrderByCreatedAtDesc(id);

        String tier      = deriveTier(user.getStatus(), orderCount);
        String riskLevel = deriveRiskLevel(ltv);

        List<CustomerDetailResponse.ActivityItem> activity = recentOrders.stream()
                .map(order -> CustomerDetailResponse.ActivityItem.builder()
                        .icon("fa-receipt")
                        .text("Placed order #" + order.getOrderNumber())
                        .time(timeAgo(order.getCreatedAt()))
                        .color("#3b82f6")
                        .build())
                .toList();

        return CustomerDetailResponse.builder()
                .id(user.getId())
                .name(buildName(user))
                .email(user.getEmail())
                .phone(user.getPhone())
                .initials(deriveInitials(user))
                .tier(tier)
                .tierClass(tier.toLowerCase())
                .orderCount(orderCount)
                .ltv(ltv)
                .riskLevel(riskLevel)
                .joinedDate(user.getCreatedAt())
                .lastSeen(timeAgo(user.getUpdatedAt()))
                .recentActivity(activity)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private CustomerSummaryResponse toSummary(User user, long orderCount, BigDecimal ltv) {
        String tier = deriveTier(user.getStatus(), orderCount);
        return CustomerSummaryResponse.builder()
                .id(user.getId())
                .name(buildName(user))
                .email(user.getEmail())
                .initials(deriveInitials(user))
                .tier(tier)
                .tierClass(tier.toLowerCase())
                .orderCount(orderCount)
                .ltv(ltv)
                .joinedDate(user.getCreatedAt())
                .build();
    }

    private String deriveTier(String status, long orderCount) {
        if ("INACTIVE".equals(status)) return "Inactive";
        if (orderCount >= 20) return "VIP";
        if (orderCount >= 5)  return "Regular";
        return "New";
    }

    private String deriveRiskLevel(BigDecimal ltv) {
        if (ltv == null) return "Low";
        if (ltv.compareTo(new BigDecimal("2000")) >= 0) return "High";
        if (ltv.compareTo(new BigDecimal("500"))  >= 0) return "Medium";
        return "Low";
    }

    private String deriveInitials(User user) {
        String first = user.getFirstName() != null && !user.getFirstName().isBlank()
                ? String.valueOf(user.getFirstName().charAt(0)).toUpperCase() : "";
        String last  = user.getLastName() != null && !user.getLastName().isBlank()
                ? String.valueOf(user.getLastName().charAt(0)).toUpperCase() : "";
        if (!first.isEmpty() || !last.isEmpty()) return first + last;
        return user.getEmail().substring(0, Math.min(2, user.getEmail().length())).toUpperCase();
    }

    private String buildName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
        String full      = (firstName + " " + lastName).trim();
        return full.isEmpty() ? user.getEmail() : full;
    }

    private String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();
        if (seconds < 60)   return seconds + " seconds ago";
        long minutes = seconds / 60;
        if (minutes < 60)   return minutes + " minutes ago";
        long hours = minutes / 60;
        if (hours < 24)     return hours + " hours ago";
        long days = hours / 24;
        if (days < 7)       return days + " days ago";
        return (days / 7) + " weeks ago";
    }

    /** Maps frontend tier/status filter value to a DB status string (or null for no filter). */
    private String resolveStatusFilter(String status) {
        if (status == null || status.isBlank()) return null;
        return "inactive".equalsIgnoreCase(status) ? "INACTIVE" : "ACTIVE";
    }
}
