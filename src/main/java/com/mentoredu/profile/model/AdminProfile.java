package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "admin_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminProfile {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(length = 80)
    private String department;
}
