package com.cfs.BMS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theaters", uniqueConstraints =
        @UniqueConstraint(name = "uk_theater_city_name", columnNames = {"city_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theater extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 300)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}
