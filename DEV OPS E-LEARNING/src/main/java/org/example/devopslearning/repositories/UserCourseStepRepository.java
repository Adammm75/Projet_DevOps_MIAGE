package org.example.devopslearning.repositories;

import java.util.List;
import java.util.Optional;

import org.example.devopslearning.entities.UserCourseStep;
import org.example.devopslearning.enums.StepName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCourseStepRepository extends JpaRepository<UserCourseStep, Long> {

    List<UserCourseStep> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<UserCourseStep> findByUserIdAndCourseIdAndStepName(
            Long userId, Long courseId, StepName stepName);
}
