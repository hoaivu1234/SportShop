package com.sport.ecommerce.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== EXCHANGE =====
    public static final String ORDER_EXCHANGE = "order.exchange";

    // ===== ROUTING KEY =====
    public static final String ORDER_EMAIL_ROUTING_KEY = "order.email";

    // ===== QUEUE =====
    public static final String ORDER_EMAIL_QUEUE = "order.email.queue";

    // ===== RETRY QUEUE =====
    public static final String ORDER_EMAIL_RETRY_QUEUE = "order.email.retry.queue";

    // ===== DLQ =====
    public static final String ORDER_EMAIL_DLQ = "order.email.dlq";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // ===== MAIN QUEUE =====
    @Bean
    public Queue orderEmailQueue() {
        return QueueBuilder.durable(ORDER_EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "order.email.retry")
                .build();
    }

    // ===== RETRY QUEUE (delay 10s) =====
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(ORDER_EMAIL_RETRY_QUEUE)
                .withArgument("x-message-ttl", 10000) // 10s retry
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_EMAIL_ROUTING_KEY)
                .build();
    }

    // ===== DLQ =====
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(ORDER_EMAIL_DLQ).build();
    }

    // ===== BINDINGS =====
    @Bean
    public Binding orderEmailBinding() {
        return BindingBuilder.bind(orderEmailQueue())
                .to(orderExchange())
                .with(ORDER_EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(orderExchange())
                .with("order.email.retry");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue())
                .to(orderExchange())
                .with("order.email.dlq");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        return factory;
    }
}
