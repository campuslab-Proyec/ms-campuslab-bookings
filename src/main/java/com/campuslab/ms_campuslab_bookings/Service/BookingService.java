package com.campuslab.ms_campuslab_bookings.Service;

import com.campuslab.ms_campuslab_bookings.Dto.BookingRequest;
import com.campuslab.ms_campuslab_bookings.Messaging.BookingEventPublisher;
import com.campuslab.ms_campuslab_bookings.Model.Booking;
import com.campuslab.ms_campuslab_bookings.Model.BookingStatus;
import com.campuslab.ms_campuslab_bookings.Repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

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

        auditClient.registerEvent(saved, "SOLICITADA", currentUser(), currentRole());
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
        auditClient.registerEvent(updated, newStatus.name(), currentUser(), currentRole());

        return updated;
    }

    public List<Booking> search(String status, String from, String to) {
        BookingStatus statusEnum = status != null ? BookingStatus.valueOf(status) : null;
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : null;
        return repository.search(statusEnum, fromDate, toDate);
    }

    private String currentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    private String currentRole() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("roles");
    }
}