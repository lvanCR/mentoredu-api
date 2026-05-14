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
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(length = 120)
    private String specialty;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus;

    @Column(columnDefinition = "text")
    private String bio;
}
