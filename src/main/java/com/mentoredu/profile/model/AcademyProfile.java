package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "academy_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademyProfile {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "academy_name", length = 120)
    private String academyName;

    @Column(length = 20, unique = true)
    private String ruc;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus;

    @Column(length = 255)
    private String website;
}
