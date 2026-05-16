package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "organization_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrganizationProfile {

    @Id
    @Column(name = "profile_id", columnDefinition = "uuid")
    private UUID profileId;

    @Column(name = "organization_name", length = 120)
    private String organizationName;

    @Column(length = 20, unique = true)
    private String ruc;

    @Column(length = 255)
    private String website;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;
}
