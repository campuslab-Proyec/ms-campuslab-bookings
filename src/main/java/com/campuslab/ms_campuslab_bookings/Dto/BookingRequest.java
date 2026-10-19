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

    @NotBlank(message = "Selecciona un laboratorio o recurso")
    private String resourceId;

    @NotBlank(message = "Indica el estudiante")
    private String studentId;

    @NotNull(message = "Indica la fecha de inicio")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime from;

    @NotNull(message = "Indica la fecha de término")
    @Future(message = "La fecha de término debe ser futura")
    private LocalDateTime to;
}
