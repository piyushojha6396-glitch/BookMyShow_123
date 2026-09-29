package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Override
    @EntityGraph(attributePaths = {"movie", "screen", "screen.theater", "screen.theater.city"})
    Page<Show> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"movie", "screen", "screen.theater", "screen.theater.city"})
    Optional<Show> findById(Long id);

    /**
     * Row-level lock used to serialise concurrent bookings of the same show,
     * which is what prevents two users from getting the same seat.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Show s where s.id = :id")
    Optional<Show> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"movie", "screen", "screen.theater", "screen.theater.city"})
    @Query("""
            select s from Show s
            where s.movie.id = :movieId
              and s.showDate >= current_date
              and (:date is null or s.showDate = :date)
              and (:cityId is null or s.screen.theater.city.id = :cityId)
            order by s.showDate, s.startTime
            """)
    List<Show> findByMovie(@Param("movieId") Long movieId,
                           @Param("date") LocalDate date,
                           @Param("cityId") Long cityId);

    @EntityGraph(attributePaths = {"movie", "screen", "screen.theater", "screen.theater.city"})
    @Query("""
            select s from Show s
            where s.screen.theater.id = :theaterId
              and s.showDate >= current_date
              and (:date is null or s.showDate = :date)
            order by s.showDate, s.startTime
            """)
    List<Show> findByTheater(@Param("theaterId") Long theaterId, @Param("date") LocalDate date);

    @EntityGraph(attributePaths = {"movie", "screen", "screen.theater", "screen.theater.city"})
    List<Show> findByScreenIdOrderByShowDateAscStartTimeAsc(Long screenId);

    /** Number of shows on the same screen/date whose time window intersects [start, end). */
    @Query("""
            select count(s) from Show s
            where s.screen.id = :screenId
              and s.showDate = :date
              and s.startTime < :end
              and s.endTime > :start
              and (:excludeId is null or s.id <> :excludeId)
            """)
    long countOverlapping(@Param("screenId") Long screenId,
                          @Param("date") LocalDate date,
                          @Param("start") LocalTime start,
                          @Param("end") LocalTime end,
                          @Param("excludeId") Long excludeId);

    boolean existsByMovieId(Long movieId);

    boolean existsByScreenId(Long screenId);
}
