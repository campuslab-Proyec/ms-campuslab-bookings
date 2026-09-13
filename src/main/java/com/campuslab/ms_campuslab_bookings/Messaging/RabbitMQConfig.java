package com.campuslab.ms_campuslab_bookings.Messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_DEAD = "cmd.dead.dlx";
    public static final String QUEUE_NOTIFY = "notify.booking.queue";
    public static final String QUEUE_NOTIFY_DLQ = "notify.booking.dlq";
    public static final String ROUTING_KEY_NOTIFY = "booking.status.changed";

    @Bean
    public DirectExchange cmdDirectExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DEAD);
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DEAD)
                .withArgument("x-dead-letter-routing-key", QUEUE_NOTIFY_DLQ)
                .build();
    }

    @Bean
    public Queue notifyDlq() {
        return QueueBuilder.durable(QUEUE_NOTIFY_DLQ).build();
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue())
                .to(cmdDirectExchange())
                .with(ROUTING_KEY_NOTIFY);
    }

    @Bean
    public Binding notifyDlqBinding() {
        return BindingBuilder.bind(notifyDlq())
                .to(deadLetterExchange())
                .with(QUEUE_NOTIFY_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}