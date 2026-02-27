package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "badge_id", "course_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    /**
     * Nullable : certains badges sont globaux, non liés à un cours précis.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Cours course;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    public void prePersist() {
        this.earnedAt = LocalDateTime.now();
    }
}