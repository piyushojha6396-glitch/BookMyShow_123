package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Screen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    @Override
    @EntityGraph(attributePaths = "theater")
    Page<Screen> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "theater")
    List<Screen> findByTheaterIdOrderByName(Long theaterId);

    boolean existsByTheaterId(Long theaterId);

    boolean existsByTheaterIdAndNameIgnoreCase(Long theaterId, String name);

    boolean existsByTheaterIdAndNameIgnoreCaseAndIdNot(Long theaterId, String name, Long id);
}
