package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.TeacherComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherCommentRepository extends JpaRepository<TeacherComment, Long> {

    /** Tous les commentaires sur un étudiant, épinglés d'abord puis par date desc */
    @Query("SELECT c FROM TeacherComment c WHERE c.student.id = :studentId " +
            "ORDER BY c.isPinned DESC, c.createdAt DESC")
    List<TeacherComment> findByStudentIdOrdered(@Param("studentId") Long studentId);

    /** Commentaires sur un étudiant pour un cours donné */
    @Query("SELECT c FROM TeacherComment c WHERE c.student.id = :studentId " +
            "AND c.course.id = :courseId ORDER BY c.isPinned DESC, c.createdAt DESC")
    List<TeacherComment> findByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /** Commentaires rédigés par un enseignant */
    List<TeacherComment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    /** Commentaires visibles par les enseignants (TEACHER_ADMIN) */
    @Query("SELECT c FROM TeacherComment c WHERE c.student.id = :studentId " +
            "AND c.visibility = 'TEACHER_ADMIN' ORDER BY c.isPinned DESC, c.createdAt DESC")
    List<TeacherComment> findVisibleByStudentId(@Param("studentId") Long studentId);

    /** Tous les commentaires (pour admin) */
    @Query("SELECT c FROM TeacherComment c ORDER BY c.createdAt DESC")
    List<TeacherComment> findAllOrdered();

    /** Recherche par tag */
    List<TeacherComment> findByTagOrderByCreatedAtDesc(String tag);

    /** Compteur de commentaires par étudiant */
    long countByStudentId(Long studentId);

    /** Compteur par cours */
    long countByCourseId(Long courseId);

    /** Supprime tous les commentaires d'un enseignant sur un étudiant dans un cours */
    void deleteByStudentIdAndTeacherIdAndCourseId(Long studentId, Long teacherId, Long courseId);
}