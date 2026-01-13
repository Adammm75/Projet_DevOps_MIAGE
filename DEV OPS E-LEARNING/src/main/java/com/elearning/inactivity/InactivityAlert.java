package com.elearning.inactivity;

import com.elearning.entities.*;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "inactivity_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InactivityAlert {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Cours course;


    @Column(name = "days_inactive", nullable = false)
    private int daysInactive;


    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;


    @Column(nullable = false, length = 20)
    private String status; // OPEN | CLOSED


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "closed_at")
    private LocalDateTime closedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;


    @PrePersist
    public void prePersist() {
    if (createdAt == null) createdAt = LocalDateTime.now();
    if (status == null) status = "OPEN";
    }
}