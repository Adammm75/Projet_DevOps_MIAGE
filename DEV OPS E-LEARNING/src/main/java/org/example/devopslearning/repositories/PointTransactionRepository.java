package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.PointTransaction;
import org.example.devopslearning.entities.PointTransaction.PointReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByStudentIdAndCourseIdOrderByEarnedAtDesc(Long studentId, Long courseId);

    /**
     * Vérifie si un point a déjà été attribué pour une référence précise (évite les doublons).
     */
    boolean existsByStudentIdAndCourseIdAndReasonAndReferenceId(
            Long studentId, Long courseId, PointReason reason, Long referenceId);

    /**
     * Nombre de transactions ATTENDANCE pour un étudiant dans un cours (= nb séances présent).
     */
    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.student.id = :studentId AND pt.course.id = :courseId AND pt.reason = 'ATTENDANCE'")
    long countAttendanceTransactions(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}