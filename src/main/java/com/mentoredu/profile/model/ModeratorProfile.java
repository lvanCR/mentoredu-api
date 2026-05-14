package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "moderator_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ModeratorProfile {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "permission_level", length = 30)
    private String permissionLevel;

    @Column
    private Boolean active = true;
}
