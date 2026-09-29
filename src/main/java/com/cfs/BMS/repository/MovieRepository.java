package com.cfs.BMS.repository;

import com.cfs.BMS.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByIdAndActiveTrue(Long id);

    @Query("""
            select m from Movie m
            where m.active = true
              and (:title is null or lower(m.title) like lower(concat('%', :title, '%')))
              and (:genre is null or lower(m.genre) = lower(:genre))
              and (:language is null or lower(m.language) = lower(:language))
            """)
    Page<Movie> search(@Param("title") String title,
                       @Param("genre") String genre,
                       @Param("language") String language,
                       Pageable pageable);

    /** Movies that have at least one show on/after the given date in the given city. */
    @Query("""
            select distinct m from Show s join s.movie m
            where m.active = true
              and s.screen.theater.city.id = :cityId
              and s.showDate >= :fromDate
            order by m.title
            """)
    List<Movie> findNowShowing(@Param("cityId") Long cityId, @Param("fromDate") LocalDate fromDate);
}
