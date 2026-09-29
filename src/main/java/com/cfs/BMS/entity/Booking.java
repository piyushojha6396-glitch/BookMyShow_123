package com.cfs.BMS.entity;

import com.cfs.BMS.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_show_status", columnList = "show_id,status"),
        @Index(name = "idx_booking_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human friendly, unique reference shown on the ticket, e.g. BMS7F3A9C21XQ. */
    @Column(unique = true, length = 20)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_seats",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    private LocalDateTime bookedAt;

    private LocalDateTime cancelledAt;

    @PrePersist
    private void onBookingCreate() {
        if (this.bookedAt == null) {
            this.bookedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = BookingStatus.CONFIRMED;
        }
    }
}
