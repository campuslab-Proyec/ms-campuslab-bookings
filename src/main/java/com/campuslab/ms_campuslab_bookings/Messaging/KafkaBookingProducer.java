package com.campuslab.ms_campuslab_bookings.Messaging;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.campuslab.ms_campuslab_bookings.Model.Booking;

@Component
public class KafkaBookingProducer {

    public static final String TOPIC = "campuslab.bookings.events";
    private static final Logger log = LoggerFactory.getLogger(KafkaBookingProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaBookingProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** Publica el estado actual de la reserva. Se envía después del commit y nunca rompe la reserva. */
    public void publish(Booking b) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("type", "BOOKING_STATUS_CHANGED");
        event.put("bookingId", b.getId());
        event.put("resourceId", b.getResourceId());
        event.put("studentId", b.getStudentId());
        event.put("status", b.getStatus().name());
        event.put("from", String.valueOf(b.getFrom()));
        event.put("to", String.valueOf(b.getTo()));
        event.put("occurredAt", LocalDateTime.now().toString());

        String key = String.valueOf(b.getId());   // misma reserva, misma partición, mismo orden
        String json = JsonUtil.toJson(event);

        Runnable send = () -> {
            try {
                kafkaTemplate.send(TOPIC, key, json).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("No se pudo publicar el evento de la reserva {}: {}", key, ex.getMessage());
                    } else {
                        log.info("Evento publicado: reserva={} partition={} offset={}", key,
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
            } catch (Exception e) {
                log.warn("Kafka no disponible, evento de la reserva {} no publicado: {}", key, e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }
}