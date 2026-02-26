package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/students")
@RequiredArgsConstructor
public class TeacherStudentsController {

    private final UserService userService;
    private final CoursService coursService;
    private final CoursClassRepository coursClassRepository;
    private final InscriptionsClassRepository inscriptionsRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final NotesCourRepository notesCourRepository;

    // ========================================
    // LISTE GLOBALE DES ÉTUDIANTS
    // ========================================

    @GetMapping
    public String studentsList(Authentication auth,
                               @RequestParam(required = false) Long courseId,
                               @RequestParam(defaultValue = "") String search,
                               Model model) {

        User teacher = userService.findByEmail(auth.getName());
        List<Cours> courses = coursService.getCoursesByTeacher(teacher);

        // Collecter tous les étudiants distincts liés aux cours de l'enseignant
        Set<User> allStudentsSet = new LinkedHashSet<>();
        for (Cours course : courses) {
            coursClassRepository.findByCoursId(course.getId()).forEach(cc -> {
                inscriptionsRepository.findByClasseId(cc.getClasse().getId())
                        .forEach(ins -> allStudentsSet.add(ins.getEtudiant()));
            });
        }

        List<User> allStudents = new ArrayList<>(allStudentsSet);

        // Filtrer par cours si sélectionné
        List<User> filteredStudents;
        Cours selectedCourse = null;
        if (courseId != null) {
            selectedCourse = coursService.getById(courseId);
            Set<User> courseStudents = new LinkedHashSet<>();
            coursClassRepository.findByCoursId(courseId).forEach(cc ->
                    inscriptionsRepository.findByClasseId(cc.getClasse().getId())
                            .forEach(ins -> courseStudents.add(ins.getEtudiant()))
            );
            filteredStudents = new ArrayList<>(courseStudents);
        } else {
            filteredStudents = allStudents;
        }

        // Filtrer par recherche
        if (!search.isBlank()) {
            String q = search.toLowerCase();
            filteredStudents = filteredStudents.stream()
                    .filter(s -> s.getFirstName().toLowerCase().contains(q)
                            || s.getLastName().toLowerCase().contains(q)
                            || s.getEmail().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        // Construire les stats par étudiant
        Long targetCourseId = courseId;
        List<StudentSummaryDTO> summaries = filteredStudents.stream()
                .map(s -> buildSummary(s, targetCourseId, courses))
                .collect(Collectors.toList());

        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        model.addAttribute("students", summaries);
        model.addAttribute("totalStudents", allStudents.size());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedCourse", selectedCourse);
        model.addAttribute("search", search);

        return "teacher/teacher-students";
    }

    // ========================================
    // FICHE DÉTAILLÉE D'UN ÉTUDIANT
    // ========================================

    @GetMapping("/{studentId}")
    public String studentDetail(@PathVariable Long studentId,
                                Authentication auth,
                                Model model) {

        User teacher = userService.findByEmail(auth.getName());
        List<Cours> teacherCourses = coursService.getCoursesByTeacher(teacher);

        // Récupérer l'étudiant
        User student = userService.findById(studentId);

        // Cours communs entre l'enseignant et l'étudiant
        List<CourseDetailDTO> courseDetails = new ArrayList<>();
        for (Cours course : teacherCourses) {
            boolean enrolled = coursClassRepository.findByCoursId(course.getId()).stream()
                    .anyMatch(cc -> inscriptionsRepository
                            .existsByClasseIdAndEtudiantId(cc.getClasse().getId(), studentId));
            if (enrolled) {
                courseDetails.add(buildCourseDetail(student, course));
            }
        }

        // Moyenne générale
        var globalAvg = notesCourRepository.calculateAverageByStudent(studentId);

        // Compteurs globaux
        long totalSubmitted = submissionRepository.countByStudentId(studentId);
        long totalGraded = submissionRepository.countGradedByStudentId(studentId);

        model.addAttribute("student", student);
        model.addAttribute("courseDetails", courseDetails);
        model.addAttribute("globalAvg", globalAvg);
        model.addAttribute("totalSubmitted", totalSubmitted);
        model.addAttribute("totalGraded", totalGraded);
        model.addAttribute("teacher", teacher);

        return "teacher/teacher-student-detail";
    }

    // ========================================
    // HELPERS — CONSTRUCTION DES DTOs
    // ========================================

    private StudentSummaryDTO buildSummary(User student, Long courseId, List<Cours> teacherCourses) {
        Long studentId = student.getId();

        // Cours concernés
        List<Cours> relevantCourses = courseId != null
                ? teacherCourses.stream().filter(c -> c.getId().equals(courseId)).collect(Collectors.toList())
                : teacherCourses;

        long totalAssignments = 0;
        long submittedAssignments = 0;
        long lateAssignments = 0;

        for (Cours course : relevantCourses) {
            List<Assignment> assignments = assignmentRepository.findByCourseId(course.getId());
            totalAssignments += assignments.size();
            for (Assignment a : assignments) {
                boolean submitted = submissionRepository
                        .existsByStudentIdAndAssignmentId(studentId, a.getId());
                if (submitted) submittedAssignments++;
                else if (a.getDueDate().isBefore(java.time.Instant.now())) lateAssignments++;
            }
        }

        var avgGrade = notesCourRepository.calculateAverageByStudent(studentId);
        long gradedCount = submissionRepository.countGradedByStudentId(studentId);

        return new StudentSummaryDTO(student, totalAssignments, submittedAssignments,
                lateAssignments, gradedCount, avgGrade);
    }

    private CourseDetailDTO buildCourseDetail(User student, Cours course) {
        Long studentId = student.getId();
        Long courseId = course.getId();

        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        long total = assignments.size();
        long submitted = assignments.stream()
                .filter(a -> submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();
        long missing = total - submitted;
        long late = assignments.stream()
                .filter(a -> a.getDueDate().isBefore(java.time.Instant.now())
                        && !submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();

        var courseGrade = notesCourRepository.calculateAverageByCourse(studentId, courseId);

        return new CourseDetailDTO(course, total, submitted, missing, late, courseGrade);
    }

    // ========================================
    // DTOs INTERNES
    // ========================================

    public static class StudentSummaryDTO {
        private final User student;
        private final long totalAssignments;
        private final long submittedAssignments;
        private final long lateAssignments;
        private final long gradedAssignments;
        private final java.math.BigDecimal avgGrade;

        public StudentSummaryDTO(User student, long totalAssignments, long submittedAssignments,
                                 long lateAssignments, long gradedAssignments, java.math.BigDecimal avgGrade) {
            this.student = student;
            this.totalAssignments = totalAssignments;
            this.submittedAssignments = submittedAssignments;
            this.lateAssignments = lateAssignments;
            this.gradedAssignments = gradedAssignments;
            this.avgGrade = avgGrade;
        }

        public User getStudent()                       { return student; }
        public long getTotalAssignments()              { return totalAssignments; }
        public long getSubmittedAssignments()          { return submittedAssignments; }
        public long getLateAssignments()               { return lateAssignments; }
        public long getGradedAssignments()             { return gradedAssignments; }
        public java.math.BigDecimal getAvgGrade()      { return avgGrade; }
        public long getMissingAssignments()            { return totalAssignments - submittedAssignments; }
        public String getStatus() {
            if (lateAssignments > 0) return "EN_RETARD";
            if (submittedAssignments == totalAssignments && totalAssignments > 0) return "A_JOUR";
            return "EN_COURS";
        }
    }

    public static class CourseDetailDTO {
        private final Cours course;
        private final long totalAssignments;
        private final long submittedAssignments;
        private final long missingAssignments;
        private final long lateAssignments;
        private final java.math.BigDecimal courseGrade;

        public CourseDetailDTO(Cours course, long totalAssignments, long submittedAssignments,
                               long missingAssignments, long lateAssignments, java.math.BigDecimal courseGrade) {
            this.course = course;
            this.totalAssignments = totalAssignments;
            this.submittedAssignments = submittedAssignments;
            this.missingAssignments = missingAssignments;
            this.lateAssignments = lateAssignments;
            this.courseGrade = courseGrade;
        }

        public Cours getCourse()                        { return course; }
        public long getTotalAssignments()               { return totalAssignments; }
        public long getSubmittedAssignments()           { return submittedAssignments; }
        public long getMissingAssignments()             { return missingAssignments; }
        public long getLateAssignments()                { return lateAssignments; }
        public java.math.BigDecimal getCourseGrade()    { return courseGrade; }
    }
}