package campuslab.ms_campuslab_bookings.dto;

import campuslab.ms_campuslab_bookings.model.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequestDTO(
        @NotNull BookingStatus status,
        String technicianId
) {}
