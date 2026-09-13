package com.campuslab.ms_campuslab_bookings.Messaging;

import com.campuslab.ms_campuslab_bookings.Model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStatusChanged(Booking booking) {
        Map<String, Object> envelope = Map.of(
                "type", "BOOKING_STATUS_CHANGED",
                "eventId", UUID.randomUUID().toString(),
                "timestamp", Instant.now().toString(),
                "traceId", UUID.randomUUID().toString(),
                "correlationId", booking.getId().toString(),
                "payload", Map.of(
                        "bookingId", booking.getId(),
                        "resourceId", booking.getResourceId(),
                        "studentId", booking.getStudentId(),
                        "status", booking.getStatus().name()
                )
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_NOTIFY,
                envelope
        );
    }
}