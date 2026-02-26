package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AttendanceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceReportRepository extends JpaRepository<AttendanceReport, Long> {

    Optional<AttendanceReport> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<AttendanceReport> findByTeacherIdOrderByGeneratedAtDesc(Long teacherId);

    List<AttendanceReport> findAllByOrderByGeneratedAtDesc();

    @Query("SELECT r FROM AttendanceReport r WHERE r.alertLevel IN ('WARNING','CRITICAL') ORDER BY r.generatedAt DESC")
    List<AttendanceReport> findAlertsOrdered();
}