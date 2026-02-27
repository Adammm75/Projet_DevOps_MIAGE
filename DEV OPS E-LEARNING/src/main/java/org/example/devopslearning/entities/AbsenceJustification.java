package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "absence_justifications")
@Data
@NoArgsConstructor
public class AbsenceJustification {

    public enum JustificationStatus { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private SessionAttendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JustificationStatus status = JustificationStatus.PENDING;

    @Column(name = "admin_comment", length = 500)
    private String adminComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}