package campuslab.ms_campuslab_bookings.rabbit.command;

public record EmailCommand(
        String eventId,
        String type,
        String timestamp,
        String traceId,
        String correlationId,
        String studentEmail,
        String subject,
        String bookingId
) {}
