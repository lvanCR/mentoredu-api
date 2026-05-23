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
    @Column(name = "profile_id", columnDefinition = "uuid")
    private UUID profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "school_name", length = 120)
    private String schoolName;

    @Column(name = "grade_level", length = 20)
    private String gradeLevel;

    @Column(name = "study_shift", length = 30)
    private String studyShift;

    @Column(name = "target_university_id", columnDefinition = "uuid")
    private UUID targetUniversityId;

    @Column(name = "target_area_id", columnDefinition = "uuid")
    private UUID targetAreaId;

    @Column(name = "target_career_id", columnDefinition = "uuid")
    private UUID targetCareerId;
}
