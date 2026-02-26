package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_points",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Cours course;

    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_level", nullable = false)
    private AcademicLevel academicLevel = AcademicLevel.DECOUVERTE;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AcademicLevel {
        DECOUVERTE,
        COMPREHENSION,
        MAITRISE
    }

    /**
     * Calcule et met à jour le niveau académique en fonction des points totaux.
     */
    public void recalculateLevel() {
        if (totalPoints >= 250) {
            this.academicLevel = AcademicLevel.MAITRISE;
        } else if (totalPoints >= 100) {
            this.academicLevel = AcademicLevel.COMPREHENSION;
        } else {
            this.academicLevel = AcademicLevel.DECOUVERTE;
        }
    }
}