package com.mentoredu.forum.model;

import com.mentoredu.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "threads")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumThread {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ThreadStatus status = ThreadStatus.OPEN;

    // Clasificación multi-modal (RN-12): mínimo uno requerido.
    // CHECK constraints en BD garantizan reglas de consistencia.

    /** Modo Institucional — requerido si se envía areaId. */
    @Column(name = "university_id", columnDefinition = "uuid")
    private UUID universityId;

    /** Modo Institucional con bloque — solo válido con universityId presente. */
    @Column(name = "area_id", columnDefinition = "uuid")
    private UUID areaId;

    /** Modo Académico — transversal, no requiere universidad. */
    @Column(name = "course_id", columnDefinition = "uuid")
    private UUID courseId;

    /** Modo Vocacional — no puede coexistir con courseId. */
    @Column(name = "career_id", columnDefinition = "uuid")
    private UUID careerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
