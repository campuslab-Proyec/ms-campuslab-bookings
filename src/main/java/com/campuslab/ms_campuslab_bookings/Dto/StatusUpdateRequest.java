package com.campuslab.ms_campuslab_bookings.Dto;

import com.campuslab.ms_campuslab_bookings.Model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {

    @NotNull
    private BookingStatus status;
}
