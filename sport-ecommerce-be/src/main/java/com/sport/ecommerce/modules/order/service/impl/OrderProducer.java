package com.sport.ecommerce.modules.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrder(Object order) {
        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.created",
                order
        );
    }
}
