package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CourseResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseResourceRepositoryy extends JpaRepository<CourseResource, Long> {

    List<CourseResource> findByCourseId(Long courseId); // si tu veux garder pour d’autres usages

    Optional<CourseResource> findTopByCourse_IdAndSummaryIsNotNullOrderByCreatedAtDesc(Long courseId);
}
