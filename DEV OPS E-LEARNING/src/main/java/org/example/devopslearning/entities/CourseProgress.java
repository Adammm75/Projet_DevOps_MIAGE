package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "course_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Cours course;

    private double progressPercentage;
    private double qcmAverage;
    private boolean completed;
    private Instant lastUpdated;
}