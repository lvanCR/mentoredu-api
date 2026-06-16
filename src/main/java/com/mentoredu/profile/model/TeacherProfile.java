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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "bio_professional", columnDefinition = "text")
    private String bioProfessional;

    @Column(name = "universities", length = 320)
    private String universities;

    @Column(name = "specialty", length = 180)
    private String specialty;

    @Column(name = "courses", length = 300)
    private String courses;

    @Column(name = "experience", length = 420)
    private String experience;

    @Column(name = "summary", length = 520)
    private String summary;
}
