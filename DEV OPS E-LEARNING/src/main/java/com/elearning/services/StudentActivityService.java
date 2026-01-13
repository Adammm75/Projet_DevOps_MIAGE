package com.elearning.services;

import com.elearning.activity.StudentActivity;
import com.elearning.entities.*;
import com.elearning.repositories.StudentActivityRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


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
            .activityTime(LocalDateTime.now())
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
