package com.campuslab.ms_campuslab_bookings.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuslab.ms_campuslab_bookings.Dto.BookingRequest;
import com.campuslab.ms_campuslab_bookings.Messaging.BookingEventPublisher;
import com.campuslab.ms_campuslab_bookings.Model.Booking;
import com.campuslab.ms_campuslab_bookings.Model.BookingStatus;
import com.campuslab.ms_campuslab_bookings.Repository.BookingRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;
    private final BookingEventPublisher eventPublisher;
    private final AuditClient auditClient;

    @Transactional
    public Booking create(BookingRequest req) {
        Booking booking = new Booking();
        booking.setResourceId(req.getResourceId());
        booking.setStudentId(req.getStudentId());
        booking.setFrom(req.getFrom());
        booking.setTo(req.getTo());
        booking.setStatus(BookingStatus.SOLICITADA);

        Booking saved = repository.save(booking);

        registerAudit(saved, "SOLICITADA");
        return saved;
    }

    public Booking findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking no encontrado: " + id));
    }

    @Transactional
    public Booking updateStatus(Long id, BookingStatus newStatus) {
        Booking booking = findById(id);
        booking.setStatus(newStatus);
        Booking updated = repository.save(booking);

        eventPublisher.publishStatusChanged(updated);
        registerAudit(updated, newStatus.name());

        return updated;
    }

    public List<Booking> search(String status, String from, String to) {
        BookingStatus statusEnum = status != null ? BookingStatus.valueOf(status) : null;
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;
        return repository.search(statusEnum, fromDate, toDate);
    }

    /**
     * Registra el evento de auditoría reenviando el JWT del usuario. La auditoría es best-effort:
     * si el servicio de audit falla, se registra el error pero no se revierte la reserva.
     */
    private void registerAudit(Booking booking, String action) {
        Jwt jwt = currentJwt();
        try {
            auditClient.registerEvent(booking, action, currentUser(jwt), currentRole(jwt), jwt.getTokenValue());
        } catch (Exception e) {
            log.warn("No se pudo registrar el evento de auditoría para la reserva {}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    private Jwt currentJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String currentUser(Jwt jwt) {
        return jwt.getSubject();
    }

    private String currentRole(Jwt jwt) {
        // En Entra ID el claim "roles" es un array; getClaimAsString devolvería null.
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && !roles.isEmpty()) {
            return String.join(",", roles);
        }
        return "UNKNOWN";
    }
}
