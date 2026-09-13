package com.campuslab.ms_campuslab_bookings.Repository;

import com.campuslab.ms_campuslab_bookings.Model.Booking;
import com.campuslab.ms_campuslab_bookings.Model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:from IS NULL OR b.from >= :from) AND " +
            "(:to IS NULL OR b.to <= :to)")
    List<Booking> search(BookingStatus status, LocalDateTime from, LocalDateTime to);
}
