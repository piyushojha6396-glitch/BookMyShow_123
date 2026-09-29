package com.cfs.BMS.service;

import com.cfs.BMS.dto.MovieRequest;
import com.cfs.BMS.dto.MovieResponse;
import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.entity.Movie;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final Clock clock;

    @Transactional
    public MovieResponse addMovie(MovieRequest request) {
        Movie movie = Movie.builder().build();
        apply(movie, request);
        return EntityMapper.toResponse(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> search(String title, String genre, String language, Pageable pageable) {
        return PageResponse.of(
                movieRepository.search(blankToNull(title), blankToNull(genre), blankToNull(language), pageable),
                EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> searchByTitle(String title) {
        return search(title, null, null, Pageable.unpaged()).content();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getByGenre(String genre) {
        return search(null, genre, null, Pageable.unpaged()).content();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getByLanguage(String language) {
        return search(null, null, language, Pageable.unpaged()).content();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getNowShowing(Long cityId) {
        return movieRepository.findNowShowing(cityId, LocalDate.now(clock)).stream()
                .map(EntityMapper::toResponse).toList();
    }

    /** Active movies only - soft-deleted movies behave as "not found". */
    @Transactional(readOnly = true)
    public Movie findEntity(Long id) {
        return movieRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = findEntity(id);
        apply(movie, request);
        return EntityMapper.toResponse(movieRepository.save(movie));
    }

    /** Soft delete so that historic shows and bookings stay intact. */
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findEntity(id);
        movie.setActive(false);
        movieRepository.save(movie);
    }

    private void apply(Movie movie, MovieRequest r) {
        movie.setTitle(r.title().trim());
        movie.setDescription(r.description());
        movie.setGenre(r.genre());
        movie.setLanguage(r.language());
        movie.setDurationMinutes(r.durationMinutes());
        movie.setRating(r.rating());
        movie.setReleaseDate(r.releaseDate());
        movie.setPosterUrl(r.posterUrl());
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
