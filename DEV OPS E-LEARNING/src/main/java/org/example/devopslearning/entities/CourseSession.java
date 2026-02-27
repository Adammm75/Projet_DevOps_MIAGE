package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "course_sessions")
@Data
@NoArgsConstructor
public class CourseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Cours course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "session_date", nullable = false)
    private Instant sessionDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "recording_url", length = 500)
    private String recordingUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}