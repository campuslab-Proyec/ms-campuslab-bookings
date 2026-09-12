package campuslab.ms_campuslab_bookings.rabbit;

import campuslab.ms_campuslab_bookings.rabbit.command.EmailCommand;
import campuslab.ms_campuslab_bookings.rabbit.command.PrepTicketCommand;
import campuslab.ms_campuslab_bookings.rabbit.command.VoucherGenCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${campuslab.rabbit.exchange-direct}")
    private String directExchange;

    public void sendEmail(String bookingId, String studentEmail, String subject, String traceId) {
        var cmd = new EmailCommand(
                UUID.randomUUID().toString(), "EMAIL_SEND", LocalDateTime.now().toString(),
                traceId, UUID.randomUUID().toString(), studentEmail, subject, bookingId
        );
        rabbitTemplate.convertAndSend(directExchange, "email.send", cmd);
    }

    public void sendPrepTicket(String bookingId, String resourceId, String technicianId, String traceId) {
        var cmd = new PrepTicketCommand(
                UUID.randomUUID().toString(), "PREP_TICKET", LocalDateTime.now().toString(),
                traceId, UUID.randomUUID().toString(), technicianId, bookingId, resourceId
        );
        rabbitTemplate.convertAndSend(directExchange, "prep.ticket", cmd);
    }

    public void sendVoucher(String bookingId, String voucherType, String traceId) {
        var cmd = new VoucherGenCommand(
                UUID.randomUUID().toString(), "VOUCHER_GEN", LocalDateTime.now().toString(),
                traceId, UUID.randomUUID().toString(), bookingId, voucherType
        );
        rabbitTemplate.convertAndSend(directExchange, "voucher.gen", cmd);
    }
}
