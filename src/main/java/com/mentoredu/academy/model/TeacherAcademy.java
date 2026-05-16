package com.mentoredu.academy.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "teacher_academies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherAcademy {

    @EmbeddedId
    private TeacherAcademyId id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class TeacherAcademyId implements Serializable {
        @Column(name = "teacher_profile_id", columnDefinition = "uuid")
        private UUID teacherProfileId;

        @Column(name = "academy_id", columnDefinition = "uuid")
        private UUID academyId;
    }
}
