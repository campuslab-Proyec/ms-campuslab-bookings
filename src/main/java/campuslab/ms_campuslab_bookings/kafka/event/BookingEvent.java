package campuslab.ms_campuslab_bookings.kafka.event;

import java.time.LocalDateTime;

public record BookingEvent(
        String eventId,
        String type,
        String bookingId,
        String studentId,
        String resourceId,
        LocalDateTime timestamp,
        String status,
        String traceId,
        String correlationId
) {}
