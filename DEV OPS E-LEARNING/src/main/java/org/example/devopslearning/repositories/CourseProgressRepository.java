package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    Optional<CourseProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseProgress> findByUserId(Long userId);
}