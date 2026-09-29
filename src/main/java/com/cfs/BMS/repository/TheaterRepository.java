package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    @Override
    @EntityGraph(attributePaths = "city")
    Page<Theater> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "city")
    List<Theater> findByCityIdOrderByName(Long cityId);

    boolean existsByCityId(Long cityId);

    boolean existsByCityIdAndNameIgnoreCase(Long cityId, String name);

    boolean existsByCityIdAndNameIgnoreCaseAndIdNot(Long cityId, String name, Long id);
}
