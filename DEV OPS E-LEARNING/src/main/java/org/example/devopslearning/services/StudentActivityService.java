package org.example.devopslearning.services;

import org.example.devopslearning.entities.*;

import jakarta.transaction.Transactional;

import org.example.devopslearning.activity.StudentActivity;
import org.example.devopslearning.repositories.StudentActivityRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Transactional
public class StudentActivityService {


    private final StudentActivityRepository repository;


    public StudentActivityService(StudentActivityRepository repository) {
        this.repository = repository;
    }


    public StudentActivity recordActivity(User student, Cours course, String activityType, com.fasterxml.jackson.databind.JsonNode metadata) {
        StudentActivity sa = StudentActivity.builder()
            .student(student)
            .course(course)
            .activityType(activityType)
            .activityTime(Instant.now())
            .metadata(metadata)
            .build();
        
        if (sa != null) {
            return repository.save(sa);
        }
        return sa;
    }


    public Optional<LocalDateTime> getLastActivityTime(Long studentId) {
        LocalDateTime dt = repository.findLastActivityTimeForStudent(studentId);
        return Optional.ofNullable(dt);
    }
}
