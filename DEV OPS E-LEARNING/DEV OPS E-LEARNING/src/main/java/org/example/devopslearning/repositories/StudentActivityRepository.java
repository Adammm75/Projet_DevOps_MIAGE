package org.example.devopslearning.repositories;

import org.example.devopslearning.activity.StudentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface StudentActivityRepository extends JpaRepository<StudentActivity, Long> {


// last activity for a student across all courses
@Query("select sa from StudentActivity sa where sa.student.id = :studentId order by sa.activityTime desc")
List<StudentActivity> findByStudentOrderByActivityTimeDesc(@Param("studentId") Long studentId);


// last activity for a student in a specific course
@Query("select sa from StudentActivity sa where sa.student.id = :studentId and sa.course.id = :courseId order by sa.activityTime desc")
List<StudentActivity> findByStudentAndCourseOrderByActivityTimeDesc(@Param("studentId") Long studentId, @Param("courseId") Long courseId);


// get the most recent activity time across all students (useful for diagnostics)
@Query("select max(sa.activityTime) from StudentActivity sa where sa.student.id = :studentId")
LocalDateTime findLastActivityTimeForStudent(@Param("studentId") Long studentId);


// fetch activities since a given timestamp (used for closing alerts when activity resumes)
List<StudentActivity> findByStudent_IdAndActivityTimeAfter(Long studentId, LocalDateTime after);
}
