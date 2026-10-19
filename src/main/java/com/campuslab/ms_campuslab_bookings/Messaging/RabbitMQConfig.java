package com.campuslab.ms_campuslab_bookings.Messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.campuslab.ms_campuslab_bookings.Model.BookingStatus;

/**
 * Topología de los eventos de reservas. bookings publica en el topic exchange booking.events con
 * routing key booking.<estado> (booking.solicitada, booking.aprobada, booking.cancelada...);
 * notify.booking.queue se enlaza con el comodín booking.* y recibe todos los cambios de estado.
 * La cola y sus bindings los declara también ms-campuslab-notify con los mismos argumentos.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_BOOKING_EVENTS = "booking.events";
    public static final String EXCHANGE_DEAD = "cmd.dead.dlx";
    public static final String QUEUE_NOTIFY = "notify.booking.queue";
    public static final String QUEUE_NOTIFY_DLQ = "notify.booking.dlq";
    /** "*" = exactamente una palabra: cualquier estado. */
    public static final String BINDING_ALL_STATUSES = "booking.*";
    public static final String ROUTING_KEY_PREFIX = "booking.";

    /** Routing key de un evento: booking.aprobada, booking.en_preparacion, etc. */
    public static String routingKeyFor(BookingStatus status) {
        return ROUTING_KEY_PREFIX + status.name().toLowerCase();
    }

    @Bean
    public TopicExchange bookingEventsExchange() {
        return new TopicExchange(EXCHANGE_BOOKING_EVENTS);
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
                .to(bookingEventsExchange())
                .with(BINDING_ALL_STATUSES);
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
