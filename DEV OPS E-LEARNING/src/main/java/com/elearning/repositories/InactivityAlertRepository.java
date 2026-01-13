package com.elearning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elearning.inactivity.InactivityAlert;

import java.util.List;


public interface InactivityAlertRepository extends JpaRepository<InactivityAlert, Long> {


List<InactivityAlert> findByStudent_IdAndCourse_IdAndStatus(Long studentId, Long courseId, String status);


List<InactivityAlert> findByStatus(String status);


List<InactivityAlert> findByCourse_IdAndStatus(Long courseId, String status);


}