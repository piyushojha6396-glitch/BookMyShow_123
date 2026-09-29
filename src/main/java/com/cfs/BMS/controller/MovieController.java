package com.cfs.BMS.controller;

import com.cfs.BMS.dto.MovieRequest;
import com.cfs.BMS.dto.MovieResponse;
import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /** Paged listing with optional filters: /api/movies?title=&genre=&language=&page=0&size=20 */
    @GetMapping
    public ResponseEntity<PageResponse<MovieResponse>> getAllMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(movieService.search(title, genre, language, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchByTitle(title));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(movieService.getByGenre(genre));
    }

    /** Fixed: was mapped to /genre/{language}, which clashed with the genre endpoint at startup. */
    @GetMapping("/language/{language}")
    public ResponseEntity<List<MovieResponse>> getByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(movieService.getByLanguage(language));
    }

    /** Movies with upcoming shows in a city. */
    @GetMapping("/now-showing")
    public ResponseEntity<List<MovieResponse>> nowShowing(@RequestParam Long cityId) {
        return ResponseEntity.ok(movieService.getNowShowing(cityId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> addMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addMovie(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
