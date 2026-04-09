package com.sport.ecommerce.modules.email.consumer;

import com.sport.ecommerce.config.rabbitmq.RabbitMQConfig;
import com.sport.ecommerce.modules.email.dto.EmailMessage;
import com.sport.ecommerce.modules.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_EMAIL_QUEUE)
    public void handleOrderEmail(EmailMessage message) {
        try {
            emailService.sendOrderSuccessEmail(message);
            log.info("Order email sent successfully");
        } catch (Exception e) {
            log.error("Send email failed, retrying...", e);
            throw e; // trigger retry
        }
    }
}
