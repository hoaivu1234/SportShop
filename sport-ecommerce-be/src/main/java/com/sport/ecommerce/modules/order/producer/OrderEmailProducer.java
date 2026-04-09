package com.sport.ecommerce.modules.order.producer;

import com.sport.ecommerce.config.rabbitmq.RabbitMQConfig;
import com.sport.ecommerce.modules.email.dto.EmailMessage;
import com.sport.ecommerce.modules.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderSuccessEmail(Order order) {
        EmailMessage message = new EmailMessage(
                "admin@gmail.com",
                order.getUser().getEmail(),
                "ORDER_SUCCESS",
                order.getId(),
                order.getUser().getFirstName() + " " + order.getUser().getLastName(),
                order.getTotalPrice()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_EMAIL_ROUTING_KEY,
                message
        );
    }
}
