package campuslab.ms_campuslab_bookings.service;

import campuslab.ms_campuslab_bookings.dto.BookingRequestDTO;
import campuslab.ms_campuslab_bookings.dto.BookingResponseDTO;
import campuslab.ms_campuslab_bookings.exception.InvalidStatusTransitionException;
import campuslab.ms_campuslab_bookings.kafka.BookingEventPublisher;
import campuslab.ms_campuslab_bookings.model.Booking;
import campuslab.ms_campuslab_bookings.model.BookingStatus;
import campuslab.ms_campuslab_bookings.rabbit.CommandPublisher;
import campuslab.ms_campuslab_bookings.repository.BookingRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository repository;
    private final BookingEventPublisher eventPublisher;
    private final CommandPublisher commandPublisher;

    // Transiciones válidas: la regla "no EN_USO sin APROBAR" queda expresada
    // porque EN_USO solo es alcanzable desde EN_PREPARACION, que a su vez
    // solo es alcanzable desde APROBADA. No existe salto directo SOLICITADA -> EN_USO.
    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = Map.of(
            BookingStatus.SOLICITADA, Set.of(BookingStatus.APROBADA, BookingStatus.CANCELADA),
            BookingStatus.APROBADA, Set.of(BookingStatus.EN_PREPARACION, BookingStatus.CANCELADA),
            BookingStatus.EN_PREPARACION, Set.of(BookingStatus.EN_USO, BookingStatus.CANCELADA),
            BookingStatus.EN_USO, Set.of(BookingStatus.DEVUELTA),
            BookingStatus.DEVUELTA, Set.of(),
            BookingStatus.CANCELADA, Set.of()
    );

    public BookingResponseDTO create(BookingRequestDTO request) {
        var booking = new Booking(
                null, request.studentId(), request.resourceId(), null,
                BookingStatus.SOLICITADA, request.startTime(), request.endTime(),
                LocalDateTime.now(), LocalDateTime.now()
        );
        booking = repository.save(booking);

        String traceId = UUID.randomUUID().toString();
        eventPublisher.publish(booking, traceId, UUID.randomUUID().toString());
        commandPublisher.sendEmail(String.valueOf(booking.getId()), request.studentId(),
                "Reserva solicitada", traceId);

        return toDTO(booking);
    }

    public BookingResponseDTO changeStatus(Long id, BookingStatus newStatus, String technicianId) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));

        if (!TRANSITIONS.get(booking.getStatus()).contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "No se puede pasar de " + booking.getStatus() + " a " + newStatus);
        }

        booking.setStatus(newStatus);
        if (technicianId != null) booking.setTechnicianId(technicianId);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = repository.save(booking);

        String traceId = UUID.randomUUID().toString();
        eventPublisher.publish(booking, traceId, UUID.randomUUID().toString());

        if (newStatus == BookingStatus.EN_PREPARACION) {
            commandPublisher.sendPrepTicket(String.valueOf(booking.getId()),
                    booking.getResourceId(), booking.getTechnicianId(), traceId);
        }
        if (newStatus == BookingStatus.EN_USO) {
            commandPublisher.sendVoucher(String.valueOf(booking.getId()), "RETIRO", traceId);
        }
        if (newStatus == BookingStatus.DEVUELTA) {
            commandPublisher.sendVoucher(String.valueOf(booking.getId()), "DEVOLUCION", traceId);
        }

        return toDTO(booking);
    }

    public BookingResponseDTO getById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
    }

    public List<BookingResponseDTO> search(BookingStatus status, LocalDateTime from, LocalDateTime to) {
        List<Booking> results = (status != null && from != null && to != null)
                ? repository.findByStatusAndStartTimeBetween(status, from, to)
                : (from != null && to != null)
                ? repository.findByStartTimeBetween(from, to)
                : (status != null)
                ? repository.findByStatus(status)
                : repository.findAll();

        return results.stream().map(this::toDTO).toList();
    }

    private BookingResponseDTO toDTO(Booking b) {
        return new BookingResponseDTO(b.getId(), b.getStudentId(), b.getResourceId(),
                b.getTechnicianId(), b.getStatus(), b.getStartTime(), b.getEndTime());
    }
}
