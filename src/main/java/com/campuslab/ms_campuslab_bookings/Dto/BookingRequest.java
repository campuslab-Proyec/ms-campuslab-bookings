package com.campuslab.ms_campuslab_bookings.Dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {

    @NotBlank
    private String resourceId;

    @NotBlank
    private String studentId;

    @NotNull
    @Future
    private LocalDateTime from;

    @NotNull
    @Future
    private LocalDateTime to;
}
