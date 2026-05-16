package com.mentoredu.library.model;

import com.mentoredu.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "academic_resources")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademicResource {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 20)
    private String visibility = "PUBLIC";

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "subject_id", columnDefinition = "uuid")
    private UUID subjectId;

    @Column(name = "institution_id", columnDefinition = "uuid")
    private UUID institutionId;

    @Column(name = "file_id", columnDefinition = "uuid")
    private UUID fileId;

    @Column(name = "exam_year")
    private Integer year;

    @Column(name = "exam_cycle", length = 30)
    private String examCycle;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (visibility == null) visibility = "PUBLIC";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
