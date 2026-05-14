package com.mentoredu.content.model;

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

    // ER: resource_type — Java field 'type' para no romper queries JPQL existentes
    @Column(name = "resource_type", nullable = false, length = 30)
    private String type;

    // ER: visibility
    @Column(nullable = false, length = 20)
    private String visibility = "PUBLIC";

    // ER: verification_status (reemplaza el booleano 'verified' anterior)
    @Column(name = "verification_status", length = 20)
    private String verificationStatus;

    // ER: verified_by
    @Column(name = "verified_by", columnDefinition = "uuid")
    private UUID verifiedBy;

    // ER: subject_id FK (nullable — se poblará en US06)
    @Column(name = "subject_id", columnDefinition = "uuid")
    private UUID subjectId;

    // ER: university_id FK (nullable — se poblará en US06)
    @Column(name = "university_id", columnDefinition = "uuid")
    private UUID universityId;

    // ER: description
    @Column(columnDefinition = "text")
    private String description;

    // ER: exam_year — Java field 'year' para no romper queries JPQL existentes
    @Column(name = "exam_year")
    private Integer year;

    // ER: exam_cycle
    @Column(name = "exam_cycle", length = 30)
    private String examCycle;

    // Campos operativos (no en el ER — compatibilidad hacia atrás, a migrar en US06)
    @Column(name = "file_url", columnDefinition = "text")
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 80)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(length = 120)
    private String university;

    @Column(length = 80)
    private String area;

    @Column(length = 120)
    private String category;

    @Column(nullable = false)
    private Boolean anonymous = false;

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
        if (anonymous == null) anonymous = false;
        if (version == null) version = 1;
        if (visibility == null) visibility = "PUBLIC";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
