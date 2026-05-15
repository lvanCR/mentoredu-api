package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teacher_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherProfile {

    @Id
    @Column(name = "profile_id", columnDefinition = "uuid")
    private UUID profileId;

    @Column(length = 120)
    private String specialty;

    @Column(name = "institution_name", length = 120)
    private String institutionName;

    @Column(name = "bio_professional", columnDefinition = "text")
    private String bioProfessional;
}
