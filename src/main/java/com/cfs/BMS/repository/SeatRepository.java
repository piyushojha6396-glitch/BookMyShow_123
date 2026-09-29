package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenIdOrderByRowAscColAsc(Long screenId);

    boolean existsByScreenIdAndSeatNumberIgnoreCase(Long screenId, String seatNumber);

    long countByScreenId(Long screenId);

    @Modifying
    @Query("delete from Seat s where s.screen.id = :screenId")
    void deleteAllByScreenId(@Param("screenId") Long screenId);
}
