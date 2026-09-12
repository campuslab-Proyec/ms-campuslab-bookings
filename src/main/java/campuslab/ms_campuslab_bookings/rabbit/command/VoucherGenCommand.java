package campuslab.ms_campuslab_bookings.rabbit.command;

public record VoucherGenCommand(
        String eventId,
        String type,
        String timestamp,
        String traceId,
        String correlationId,
        String bookingId,
        String voucherType
) {}
