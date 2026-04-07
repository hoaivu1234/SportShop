package com.sport.ecommerce.modules.order.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    @RabbitListener(queues = "order.queue")
    public void handleOrder(Object order) {
        System.out.println("Received: " + order);

        // xử lý logic
    }
}
