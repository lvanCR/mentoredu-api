package com.mentoredu.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private List<Area> areas;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private List<Career> careers;
}
