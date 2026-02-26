package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.SessionAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.List;
import java.time.Instant;
import org.springframework.data.repository.query.Param;

@Repository
public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, Long> {

    List<SessionAttendance> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    @Query("SELECT COUNT(a) FROM SessionAttendance a WHERE a.student.id = :studentId AND a.session.course.id = :courseId AND a.status = 'ABSENT'")
    long countAbsencesByStudentAndCourse(Long studentId, Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SessionAttendance a WHERE a.session.id = :sessionId")
    void deleteBySessionId(Long sessionId);

    @Query("SELECT a FROM SessionAttendance a WHERE a.student.id = :studentId AND a.markedAt >= :since ORDER BY a.markedAt DESC")
    List<SessionAttendance> findRecentByStudentId(@Param("studentId") Long studentId, @Param("since") Instant since);
    // Méthode pour récupérer les absences/retards d'un étudiant
    @Query("SELECT a FROM SessionAttendance a WHERE a.student.id = :studentId AND a.status IN :statuses")
    List<SessionAttendance> findByStudentIdAndStatusIn(
            @Param("studentId") Long studentId,
            @Param("statuses") List<SessionAttendance.AttendanceStatus> statuses);

    @Query("""
    SELECT COUNT(sa)
    FROM SessionAttendance sa
    WHERE sa.session.course.id = :courseId
      AND sa.student.id        = :studentId
""")
    long countByCourseIdAndStudentId(@Param("courseId") Long courseId,
                                     @Param("studentId") Long studentId);
}