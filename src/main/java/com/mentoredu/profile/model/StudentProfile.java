package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "school_name", length = 120)
    private String schoolName;

    @Column(name = "grade_level", length = 20)
    private String gradeLevel;

    @Column(name = "target_university", length = 120)
    private String targetUniversity;

    @Column(name = "target_career", length = 120)
    private String targetCareer;

    @Column(name = "study_shift", length = 30)
    private String studyShift;

    @Column(name = "is_anonymous_allowed")
    private Boolean isAnonymousAllowed = true;
}
