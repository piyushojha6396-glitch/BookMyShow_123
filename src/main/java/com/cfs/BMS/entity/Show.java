package com.cfs.BMS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "shows", indexes = {
        @Index(name = "idx_show_screen_date", columnList = "screen_id,show_date"),
        @Index(name = "idx_show_movie_date", columnList = "movie_id,show_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private LocalDate showDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /** Base price; the final seat price = base * SeatType multiplier. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;
}
