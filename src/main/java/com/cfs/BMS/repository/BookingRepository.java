package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Booking;
import com.cfs.BMS.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Optional<Booking> findById(Long id);

    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Optional<Booking> findByBookingReference(String bookingReference);

    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Page<Booking> findByShowId(Long showId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "show", "show.movie", "show.screen", "show.screen.theater", "show.screen.theater.city"})
    Page<Booking> findAll(Pageable pageable);

    /** Ids of all seats held by CONFIRMED bookings of the given show. */
    @Query("select s.id from Booking b join b.seats s where b.show.id = :showId and b.status = :status")
    List<Long> findSeatIdsByShowIdAndStatus(@Param("showId") Long showId, @Param("status") BookingStatus status);

    boolean existsByShowId(Long showId);

    boolean existsByShowIdAndStatus(Long showId, BookingStatus status);

    @Query("select count(b) from Booking b join b.seats s where s.id = :seatId")
    long countBySeatId(@Param("seatId") Long seatId);
}
