package org.example.devopslearning;

import org.example.devopslearning.entities.UserCourseStep;
import org.example.devopslearning.enums.StepName;
import org.example.devopslearning.enums.StepStatus;
import org.example.devopslearning.repositories.UserCourseStepRepository;
import org.example.devopslearning.services.CourseStepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseStepServiceTest {

    @Autowired
    private CourseStepService courseStepService;

    @Autowired
    private UserCourseStepRepository repository;

    private final Long userId = 21L;
    private final Long courseId = 200L;

    @Test
    void shouldInitializeStepsIfEmpty() {

        List<UserCourseStep> steps =
                courseStepService.initializeStepsIfNeeded(userId, courseId);

        assertEquals(5, steps.size());

        UserCourseStep summary =
                repository.findByUserIdAndCourseIdAndStepName(
                        userId, courseId, StepName.SUMMARY
                ).orElseThrow();

        assertEquals(StepStatus.IN_PROGRESS, summary.getStatus());
    }

    @Test
    void shouldMarkStepAsDoneAndUnlockNext() {

        courseStepService.initializeStepsIfNeeded(userId, courseId);

        courseStepService.markStepAsDone(
                userId,
                courseId,
                StepName.SUMMARY
        );

        UserCourseStep summary =
                repository.findByUserIdAndCourseIdAndStepName(
                        userId, courseId, StepName.SUMMARY
                ).orElseThrow();

        UserCourseStep next =
                repository.findByUserIdAndCourseIdAndStepName(
                        userId, courseId, StepName.COURSE_CONTENT
                ).orElseThrow();

        assertEquals(StepStatus.DONE, summary.getStatus());
        assertEquals(StepStatus.IN_PROGRESS, next.getStatus());
    }

    @Test
    void shouldCalculateProgressCorrectly() {

        courseStepService.initializeStepsIfNeeded(userId, courseId);

        courseStepService.markStepAsDone(userId, courseId, StepName.SUMMARY);
        courseStepService.markStepAsDone(userId, courseId, StepName.COURSE_CONTENT);

        double progress =
                courseStepService.calculateProgress(userId, courseId);

        assertEquals(40.0, progress);
    }


    @Test
    void shouldNotDuplicateSteps() {

        courseStepService.initializeStepsIfNeeded(userId, courseId);
        courseStepService.initializeStepsIfNeeded(userId, courseId);

        List<UserCourseStep> steps =
                repository.findByUserIdAndCourseId(userId, courseId);

        assertEquals(5, steps.size());
    }
}
