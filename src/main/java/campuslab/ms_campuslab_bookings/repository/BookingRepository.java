package campuslab.ms_campuslab_bookings.repository;

import campuslab.ms_campuslab_bookings.model.Booking;
import campuslab.ms_campuslab_bookings.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndStartTimeBetween(
            BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    List<Booking> findByStatus(BookingStatus status);
}
