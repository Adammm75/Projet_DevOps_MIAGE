package org.example.devopslearning.services;

import java.time.Instant;
import java.util.List;

import org.example.devopslearning.entities.UserCourseStep;
import org.example.devopslearning.enums.StepName;
import org.example.devopslearning.enums.StepStatus;
import org.example.devopslearning.repositories.UserCourseStepRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseStepService {

    private final UserCourseStepRepository repository;

    public List<UserCourseStep> initializeStepsIfNeeded(Long userId, Long courseId) {

        List<UserCourseStep> steps =
                repository.findByUserIdAndCourseId(userId, courseId);

        if (steps.isEmpty()) {

            for (StepName step : StepName.values()) {

                UserCourseStep newStep = UserCourseStep.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .stepName(step)
                        .status(step == StepName.SUMMARY ? StepStatus.IN_PROGRESS : StepStatus.TODO)
                        .lastUpdated(Instant.now())
                        .build();

                repository.save(newStep);
            }

            steps = repository.findByUserIdAndCourseId(userId, courseId);
        }

        return steps;
    }

    public void markStepAsDone(Long userId, Long courseId, StepName stepName) {

        UserCourseStep step = repository
                .findByUserIdAndCourseIdAndStepName(userId, courseId, stepName)
                .orElseThrow();

        step.setStatus(StepStatus.DONE);
        step.setLastUpdated(Instant.now());
        repository.save(step);

        unlockNextStep(userId, courseId, stepName);
    }

    private void unlockNextStep(Long userId, Long courseId, StepName current) {

        StepName[] steps = StepName.values();

        for (int i = 0; i < steps.length; i++) {
            if (steps[i] == current && i + 1 < steps.length) {

                StepName next = steps[i + 1];

                UserCourseStep nextStep =
                        repository.findByUserIdAndCourseIdAndStepName(userId, courseId, next)
                                .orElseThrow();

                if (nextStep.getStatus() == StepStatus.TODO) {
                    nextStep.setStatus(StepStatus.IN_PROGRESS);
                    repository.save(nextStep);
                }
                break;
            }
        }
    }

    public double calculateProgress(Long userId, Long courseId) {

        List<UserCourseStep> steps =
                repository.findByUserIdAndCourseId(userId, courseId);

        long doneCount = steps.stream()
                .filter(s -> s.getStatus() == StepStatus.DONE)
                .count();

        return (doneCount * 100.0) / StepName.values().length;
    }
}
