package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Log de consultation d'une ressource par un étudiant.
 * Créé la première fois que l'étudiant ouvre la ressource.
 */
@Getter
@Setter
@Entity
@Table(name = "resource_consultations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"resource_id", "student_id"}
        ))
public class ResourceConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private CourseRessource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "consulted_at", nullable = false)
    private Instant consultedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 1;

    @PrePersist
    protected void onCreate() {
        if (consultedAt == null) consultedAt = Instant.now();
        if (lastSeenAt == null) lastSeenAt = consultedAt;
    }
}