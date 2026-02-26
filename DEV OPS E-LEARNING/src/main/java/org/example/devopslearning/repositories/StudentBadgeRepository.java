package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.StudentBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentBadgeRepository extends JpaRepository<StudentBadge, Long> {

    List<StudentBadge> findByStudentId(Long studentId);

    List<StudentBadge> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndBadgeIdAndCourseId(Long studentId, Long badgeId, Long courseId);

    /**
     * Tous les badges obtenus pour un cours (vue enseignant).
     */
    List<StudentBadge> findByCourseId(Long courseId);
}