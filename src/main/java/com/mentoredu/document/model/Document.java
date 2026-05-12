package com.mentoredu.document.model;

import com.mentoredu.auth.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String type;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotBlank
    @Column(nullable = false)
    private String fileUrl;

    @NotBlank
    @Column(nullable = false)
    private String university;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @NotBlank
    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private Boolean verified;

    @Column(nullable = false)
    private Boolean anonymous;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (verified == null) verified = false;
        if (anonymous == null) anonymous = false;
    }
}
