package campuslab.ms_campuslab_bookings.rabbit.command;

public record PrepTicketCommand(
        String eventId,
        String type,
        String timestamp,
        String traceId,
        String correlationId,
        String technicianId,
        String bookingId,
        String resourceId
) {}