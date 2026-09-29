package com.cfs.BMS.entity;

import com.cfs.BMS.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints =
        @UniqueConstraint(name = "uk_seat_screen_number", columnNames = {"screen_id", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    /** A, B, C ... */
    @Column(name = "seat_row", length = 5)
    private String row;

    /** 1, 2, 3 ... */
    @Column(name = "seat_col")
    private Integer col;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
