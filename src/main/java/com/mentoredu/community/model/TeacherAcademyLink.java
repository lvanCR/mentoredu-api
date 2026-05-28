package com.mentoredu.community.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "teacher_academy_links",
    uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_profile_id", "academy_profile_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherAcademyLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_profile_id", nullable = false, columnDefinition = "uuid")
    private UUID teacherProfileId;

    @Column(name = "academy_profile_id", nullable = false, columnDefinition = "uuid")
    private UUID academyProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssociationStatus status = AssociationStatus.PENDING;

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
