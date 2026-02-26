package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "attendance_reports")
@Data
@NoArgsConstructor
public class AttendanceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private CourseSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "total_students", nullable = false)
    private int totalStudents;

    @Column(name = "present_count", nullable = false)
    private int presentCount;

    @Column(name = "absent_count", nullable = false)
    private int absentCount;

    @Column(name = "late_count", nullable = false)
    private int lateCount;

    @Column(name = "presence_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal presenceRate = BigDecimal.ZERO;

    @Column(name = "alert_level", nullable = false, length = 20)
    private String alertLevel = "NORMAL";

    @Column(name = "sent_to_admin", nullable = false)
    private boolean sentToAdmin = false;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}