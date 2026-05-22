package com.mentoredu.library.model;

import com.mentoredu.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(name = "university_id", nullable = false, columnDefinition = "uuid")
    private UUID universityId;

    @Column(name = "area_id", nullable = false, columnDefinition = "uuid")
    private UUID areaId;

    /** Nullable — refinamiento vocacional opcional. RN-23: career.area_id == area_id. */
    @Column(name = "career_id", columnDefinition = "uuid")
    private UUID careerId;

    /** Nullable — obligatorio salvo para EXAMEN_COMPLETO, GUIA y APUNTES. RN-07. */
    @Column(name = "course_id", columnDefinition = "uuid")
    private UUID courseId;

    @Column(name = "file_url", columnDefinition = "text")
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "mime_type", length = 80)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(nullable = false, length = 20)
    private String visibility;

    @Column(name = "acepta_resoluciones", nullable = false)
    private boolean aceptaResoluciones;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
