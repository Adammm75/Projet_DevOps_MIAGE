package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.UserCourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCourseCompletionRepository extends JpaRepository<UserCourseCompletion, Long> {

    /** Trouve le marqueur d'un étudiant pour un cours */
    Optional<UserCourseCompletion> findByUserAndCourse(User user, Cours course);

    /** L'étudiant a-t-il marqué ce cours comme terminé ? */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM UserCourseCompletion c " +
            "WHERE c.user.id = :userId AND c.course.id = :courseId AND c.completed = true")
    boolean isCompletedByStudent(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /** Nombre d'étudiants ayant marqué le cours comme terminé */
    @Query("SELECT COUNT(c) FROM UserCourseCompletion c " +
            "WHERE c.course.id = :courseId AND c.completed = true")
    long countCompletedByCourse(@Param("courseId") Long courseId);

    /** ✅ AJOUTÉ : Nombre total de déclarations "cours terminé" sur toute la plateforme */
    @Query("SELECT COUNT(c) FROM UserCourseCompletion c WHERE c.completed = true")
    long countAllCompleted();
}