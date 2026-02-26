package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AbsenceJustification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceJustificationRepository extends JpaRepository<AbsenceJustification, Long> {

    List<AbsenceJustification> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<AbsenceJustification> findByStatusOrderBySubmittedAtDesc(AbsenceJustification.JustificationStatus status);

    List<AbsenceJustification> findAllByOrderBySubmittedAtDesc();

    Optional<AbsenceJustification> findByAttendanceId(Long attendanceId);

    boolean existsByAttendanceId(Long attendanceId);

    long countByStatus(AbsenceJustification.JustificationStatus status);

    @Query("SELECT j FROM AbsenceJustification j WHERE j.student.id = :studentId AND j.status = :status")
    List<AbsenceJustification> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                                        @Param("status") AbsenceJustification.JustificationStatus status);
}