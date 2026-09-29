package com.cfs.BMS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_title", columnList = "title"),
        @Index(name = "idx_movie_genre", columnList = "genre"),
        @Index(name = "idx_movie_language", columnList = "language")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 50)
    private String genre;

    @Column(length = 50)
    private String language;

    private Integer durationMinutes;

    private Double rating;

    private LocalDate releaseDate;

    @Column(length = 500)
    private String posterUrl;

    /** Soft-delete flag: movies with historic shows/bookings are hidden, never physically removed. */
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    @Builder.Default
    private boolean active = true;
}
