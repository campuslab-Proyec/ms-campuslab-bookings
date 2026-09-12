package campuslab.ms_campuslab_bookings.kafka;

import campuslab.ms_campuslab_bookings.kafka.event.BookingEvent;
import campuslab.ms_campuslab_bookings.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${campuslab.kafka.topic-events}")
    private String eventsTopic;

    public void publish(Booking booking, String traceId, String correlationId) {
        var event = new BookingEvent(
                UUID.randomUUID().toString(),
                "BOOKING_" + booking.getStatus().name(),
                String.valueOf(booking.getId()),
                booking.getStudentId(),
                booking.getResourceId(),
                LocalDateTime.now(),
                booking.getStatus().name(),
                traceId,
                correlationId
        );
        kafkaTemplate.send(eventsTopic, String.valueOf(booking.getId()), event);
    }
}
