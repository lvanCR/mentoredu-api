package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academy_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademyProfile {

    @Id
    @Column(name = "profile_id", columnDefinition = "uuid")
    private java.util.UUID profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "academy_name", nullable = false, length = 120, unique = true)
    private String academyName;

    @Column(length = 20, unique = true)
    private String ruc;

    @Column(length = 255)
    private String website;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;
}
