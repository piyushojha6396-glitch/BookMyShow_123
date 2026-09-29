package com.cfs.BMS.entity;

import com.cfs.BMS.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt hash - never the raw password. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) not null default 'USER'")
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    @Builder.Default
    private boolean active = true;
}
