package campuslab.ms_campuslab_bookings.dto;

import campuslab.ms_campuslab_bookings.model.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponseDTO(
        Long id,
        String studentId,
        String resourceId,
        String technicianId,
        BookingStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
