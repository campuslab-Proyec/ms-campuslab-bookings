package campuslab.ms_campuslab_bookings.controller;

import campuslab.ms_campuslab_bookings.dto.BookingRequestDTO;
import campuslab.ms_campuslab_bookings.dto.BookingResponseDTO;
import campuslab.ms_campuslab_bookings.dto.StatusUpdateRequestDTO;
import campuslab.ms_campuslab_bookings.model.BookingStatus;
import campuslab.ms_campuslab_bookings.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(@Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @GetMapping("/{id}")
    public BookingResponseDTO getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PutMapping("/{id}/status")
    public BookingResponseDTO updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusUpdateRequestDTO request) {
        return bookingService.changeStatus(id, request.status(), request.technicianId());
    }

    @GetMapping
    public List<BookingResponseDTO> search(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return bookingService.search(status, from, to);
    }
}
