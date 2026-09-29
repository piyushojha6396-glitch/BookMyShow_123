package com.cfs.BMS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screens", uniqueConstraints =
        @UniqueConstraint(name = "uk_screen_theater_name", columnNames = {"theater_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. AUDI-1, AUDI-2 */
    @Column(nullable = false, length = 100)
    private String name;

    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;
}
