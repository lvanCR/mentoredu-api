package com.mentoredu.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "institutions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Institution {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 80)
    private String city;
}
