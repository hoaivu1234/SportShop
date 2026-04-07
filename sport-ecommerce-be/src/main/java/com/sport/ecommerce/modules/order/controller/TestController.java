package com.sport.ecommerce.modules.order.controller;

import com.sport.ecommerce.modules.order.service.impl.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final OrderProducer producer;

    @GetMapping("/test-rabbit")
    public String test() {
        producer.sendOrder("Hello RabbitMQ");
        return "Sent!";
    }
}
