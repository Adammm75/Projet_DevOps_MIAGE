package org.example.devopslearning;

import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.example.devopslearning.services.CourseProgressService;
import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CourseProgressServiceTest {

    @Test
    void shouldCalculateProgressCorrectly() {

        CourseResourceRepository resourceRepo = mock(CourseResourceRepository.class);
        AssignmentRepository assignmentRepo = mock(AssignmentRepository.class);
        QcmRepository qcmRepo = mock(QcmRepository.class);
        QcmTentativeRepository tentativeRepo = mock(QcmTentativeRepository.class);
        CourseProgressRepository progressRepo = mock(CourseProgressRepository.class);

        CourseProgressService service = new CourseProgressService(
                progressRepo,
                resourceRepo,
                assignmentRepo,
                qcmRepo,
                tentativeRepo
        );

        User user = new User();
        user.setId(1L);

        Cours course = new Cours();
        course.setId(1L);

        when(resourceRepo.countByCourse(course)).thenReturn(10L);
        when(assignmentRepo.countByCourse(course)).thenReturn(5L);
        when(assignmentRepo.countGradedAssignmentsByCourse(1L)).thenReturn(5L);
        when(qcmRepo.findByCoursId(1L)).thenReturn(List.of());

        when(progressRepo.save(any(CourseProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseProgress result = service.updateProgress(user, course);

        assertNotNull(result);
        assertTrue(result.getProgressPercentage() > 0);
    }
}