package com.elearning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.elearning.entities.Cours;

import java.util.List;


public interface CourseRepository extends JpaRepository<Cours, Long> {

	// custom method to find courses where a student is enrolled
	// use CourseEnrollment join entity instead of a non-existent 'enrollments' field
	@Query("select e.course from CourseEnrollment e where e.student.id = :studentId")
	List<Cours> findCoursesForStudent(@Param("studentId") Long studentId);


	// custom method to get teachers for a course
	// use CourseTeacher join entity (teacher relation is stored there)
	@Query("select ct.teacher from CourseTeacher ct where ct.course.id = :courseId")
	List<com.elearning.entities.User> findTeachersForCourse(@Param("courseId") Long courseId);


	// find courses by teacher id via CourseTeacher join entity
	@Query("select ct.course from CourseTeacher ct where ct.teacher.id = :teacherId")
	List<Cours> findCoursesByTeacherId(@Param("teacherId") Long teacherId);

}
