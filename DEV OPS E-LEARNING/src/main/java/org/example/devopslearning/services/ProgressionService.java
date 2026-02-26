package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service central de progression pédagogique.
 * Intègre : ressources, devoirs, notes et QCM.
 */
@Service
@RequiredArgsConstructor
public class ProgressionService {

    private final CourseAccessService courseAccessService;
    private final CourseResourceRepository resourceRepository;
    private final ResourceConsultationRepository consultationRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final QcmRepository qcmRepository;
    private final QcmTentativeRepository tentativeRepository;
    private final UserCourseCompletionRepository completionRepository;
    private final InscriptionsClassRepository inscriptionsRepository;
    private final CoursClassRepository coursClassRepository;
    private final NotesCourRepository notesCourRepository;

    // ========================================
    // CÔTÉ ÉTUDIANT
    // ========================================

    public List<CourseProgressDTO> getStudentProgression(Long studentId) {
        List<Cours> courses = courseAccessService.getCoursAccessibles(studentId);
        List<CourseProgressDTO> result = new ArrayList<>();
        for (Cours course : courses) {
            result.add(buildCourseProgress(studentId, course));
        }
        result.sort((a, b) -> Integer.compare(b.getProgressPercent(), a.getProgressPercent()));
        return result;
    }

    public int getGlobalProgressPercent(Long studentId) {
        List<CourseProgressDTO> progressions = getStudentProgression(studentId);
        if (progressions.isEmpty()) return 0;
        return (int) progressions.stream()
                .mapToInt(CourseProgressDTO::getProgressPercent)
                .average().orElse(0);
    }

    public BigDecimal getGlobalAverage(Long studentId) {
        BigDecimal avg = notesCourRepository.calculateAverageByStudent(studentId);
        return avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public CourseProgressDTO buildCourseProgress(Long studentId, Cours course) {
        Long courseId = course.getId();

        // --- Ressources ---
        long totalResources = resourceRepository.countByCourse_Id(courseId);
        long consultedResources = consultationRepository.countConsultedByStudentInCourse(studentId, courseId);
        int resourceScore = totalResources > 0 ? (int)(consultedResources * 100 / totalResources) : 0;

        // --- Devoirs ---
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        long totalAssignments = assignments.size();
        long submittedAssignments = assignments.stream()
                .filter(a -> submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();
        long gradedAssignments = assignments.stream()
                .filter(a -> {
                    Optional<AssignmentSubmission> sub = submissionRepository
                            .findByStudentIdAndAssignmentId(studentId, a.getId());
                    return sub.isPresent() && sub.get().getGrade() != null;
                })
                .count();
        long lateAssignments = assignments.stream()
                .filter(a -> a.getDueDate().isBefore(java.time.Instant.now())
                        && !submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();
        int assignmentScore = totalAssignments > 0
                ? (int)(submittedAssignments * 100 / totalAssignments) : 0;

        // --- Note du cours ---
        BigDecimal courseGrade = notesCourRepository
                .calculateAverageByCourse(studentId, courseId);
        if (courseGrade != null) courseGrade = courseGrade.setScale(2, RoundingMode.HALF_UP);

        // Mention basée sur la note
        String mention = null;
        if (courseGrade != null) {
            double g = courseGrade.doubleValue();
            if (g >= 16) mention = "TB";
            else if (g >= 14) mention = "B";
            else if (g >= 12) mention = "AB";
            else if (g >= 10) mention = "P";
            else mention = "F";
        }

        // --- QCM ---
        List<Qcm> qcms = qcmRepository.findByCoursIdAndPublie(courseId, true);
        long totalQcm = qcms.size();
        long completedQcm = qcms.stream()
                .filter(q -> tentativeRepository.countByEtudiantIdAndQcmId(studentId, q.getId()) > 0)
                .count();
        int qcmScore = totalQcm > 0 ? (int)(completedQcm * 100 / totalQcm) : 0;

        // --- Progression pondérée ---
        int progressPercent;
        if (totalResources == 0 && totalAssignments == 0 && totalQcm == 0) {
            progressPercent = 0;
        } else {
            int weight = 0, score = 0;
            if (totalResources > 0)   { score += resourceScore * 35;   weight += 35; }
            if (totalAssignments > 0) { score += assignmentScore * 40;  weight += 40; }
            if (totalQcm > 0)         { score += qcmScore * 25;         weight += 25; }
            progressPercent = weight > 0 ? score / weight : 0;
        }

        // --- Cours terminé ---
        boolean completed = completionRepository.isCompletedByStudent(studentId, courseId);

        // --- Message motivant ---
        String motivationMessage = buildMotivationMessage(
                course.getTitle(), progressPercent, completed, courseGrade);

        return new CourseProgressDTO(
                course, progressPercent,
                totalResources, consultedResources,
                totalAssignments, submittedAssignments, gradedAssignments, lateAssignments,
                totalQcm, completedQcm,
                courseGrade, mention,
                completed, motivationMessage
        );
    }

    private String buildMotivationMessage(String title, int percent, boolean completed, BigDecimal grade) {
        if (completed) return "✅ Tu as terminé " + title + " — bravo pour ton investissement !";
        if (grade != null && grade.doubleValue() >= 16)
            return "🏆 Excellent ! Tu as " + grade + "/20 sur " + title + " — continue ainsi !";
        if (grade != null && grade.doubleValue() < 10)
            return "💡 Tu as " + grade + "/20 sur " + title + " — il reste de la marge, accroche-toi !";
        if (percent >= 80) return "🚀 Tu es à " + percent + "% sur " + title + " — la ligne d'arrivée est proche !";
        if (percent >= 50) return "💪 Tu as complété " + percent + "% de " + title + " — continue comme ça !";
        if (percent >= 20) return "📖 Tu avances sur " + title + " (" + percent + "%) — chaque étape compte !";
        return "🌱 Lance-toi sur " + title + " — le premier pas est le plus important !";
    }

    // ========================================
    // CÔTÉ ENSEIGNANT
    // ========================================

    public List<StudentProgressDTO> getCourseProgressionForTeacher(Long courseId) {
        List<InscriptionsClass> inscriptions = getStudentsEnrolledInCourse(courseId);
        return inscriptions.stream()
                .map(ins -> buildStudentProgress(ins.getEtudiant(), courseId))
                .sorted(Comparator.comparingInt(StudentProgressDTO::getProgressPercent).reversed())
                .collect(Collectors.toList());
    }

    public StudentProgressDTO buildStudentProgress(User student, Long courseId) {
        Long studentId = student.getId();

        // Ressources
        long totalResources = resourceRepository.countByCourse_Id(courseId);
        long consulted = consultationRepository.countConsultedByStudentInCourse(studentId, courseId);

        // Devoirs
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        long totalAssignments = assignments.size();
        long submitted = assignments.stream()
                .filter(a -> submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();
        long missing = totalAssignments - submitted;
        long graded = assignments.stream()
                .filter(a -> {
                    Optional<AssignmentSubmission> sub = submissionRepository
                            .findByStudentIdAndAssignmentId(studentId, a.getId());
                    return sub.isPresent() && sub.get().getGrade() != null;
                })
                .count();
        long late = assignments.stream()
                .filter(a -> a.getDueDate().isBefore(java.time.Instant.now())
                        && !submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .count();

        // Note du cours
        BigDecimal courseGrade = notesCourRepository.calculateAverageByCourse(studentId, courseId);
        if (courseGrade != null) courseGrade = courseGrade.setScale(2, RoundingMode.HALF_UP);

        // Progression
        int progress = 0;
        int w = 0, s = 0;
        if (totalResources > 0)   { s += (int)(consulted * 100 / totalResources) * 35;  w += 35; }
        if (totalAssignments > 0) { s += (int)(submitted * 100 / totalAssignments) * 40; w += 40; }
        if (w > 0) progress = s / w;

        boolean completed = completionRepository.isCompletedByStudent(studentId, courseId);

        String status;
        if (completed)         status = "TERMINE";
        else if (progress >= 50) status = "EN_AVANCE";
        else if (progress >= 20) status = "EN_COURS";
        else                     status = "INACTIF";

        return new StudentProgressDTO(
                student, progress,
                submitted, missing, graded, late,
                consulted, totalResources,
                courseGrade, completed, status
        );
    }

    public int getAverageCourseProgression(Long courseId) {
        List<StudentProgressDTO> list = getCourseProgressionForTeacher(courseId);
        if (list.isEmpty()) return 0;
        return (int) list.stream().mapToInt(StudentProgressDTO::getProgressPercent).average().orElse(0);
    }

    public Map<String, Long> getProgressionStats(Long courseId) {
        List<StudentProgressDTO> list = getCourseProgressionForTeacher(courseId);
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("EN_AVANCE", list.stream().filter(s -> "EN_AVANCE".equals(s.getStatus())).count());
        stats.put("EN_COURS",  list.stream().filter(s -> "EN_COURS".equals(s.getStatus())).count());
        stats.put("INACTIF",   list.stream().filter(s -> "INACTIF".equals(s.getStatus())).count());
        stats.put("TERMINE",   list.stream().filter(s -> "TERMINE".equals(s.getStatus())).count());
        return stats;
    }

    // Moyenne de classe pour un cours
    public Double getCourseClassAverage(Long courseId) {
        return notesCourRepository.getAverageGradeByCourseId(courseId);
    }

    // ========================================
    // HELPERS
    // ========================================

    private List<InscriptionsClass> getStudentsEnrolledInCourse(Long courseId) {
        List<Long> classeIds = coursClassRepository.findByCoursId(courseId).stream()
                .map(cc -> cc.getClasse().getId())
                .collect(Collectors.toList());
        return classeIds.stream()
                .flatMap(cid -> inscriptionsRepository.findByClasseId(cid).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    // ========================================
    // DTO ÉTUDIANT
    // ========================================

    public static class CourseProgressDTO {
        private final Cours course;
        private final int progressPercent;
        private final long totalResources;
        private final long consultedResources;
        private final long totalAssignments;
        private final long submittedAssignments;
        private final long gradedAssignments;
        private final long lateAssignments;
        private final long totalQcm;
        private final long completedQcm;
        private final BigDecimal courseGrade;
        private final String mention;
        private final boolean courseCompleted;
        private final String motivationMessage;

        public CourseProgressDTO(Cours course, int progressPercent,
                                 long totalResources, long consultedResources,
                                 long totalAssignments, long submittedAssignments,
                                 long gradedAssignments, long lateAssignments,
                                 long totalQcm, long completedQcm,
                                 BigDecimal courseGrade, String mention,
                                 boolean courseCompleted, String motivationMessage) {
            this.course = course;
            this.progressPercent = progressPercent;
            this.totalResources = totalResources;
            this.consultedResources = consultedResources;
            this.totalAssignments = totalAssignments;
            this.submittedAssignments = submittedAssignments;
            this.gradedAssignments = gradedAssignments;
            this.lateAssignments = lateAssignments;
            this.totalQcm = totalQcm;
            this.completedQcm = completedQcm;
            this.courseGrade = courseGrade;
            this.mention = mention;
            this.courseCompleted = courseCompleted;
            this.motivationMessage = motivationMessage;
        }

        public Cours getCourse()                   { return course; }
        public int getProgressPercent()            { return progressPercent; }
        public long getTotalResources()            { return totalResources; }
        public long getConsultedResources()        { return consultedResources; }
        public long getTotalAssignments()          { return totalAssignments; }
        public long getSubmittedAssignments()      { return submittedAssignments; }
        public long getGradedAssignments()         { return gradedAssignments; }
        public long getLateAssignments()           { return lateAssignments; }
        public long getTotalQcm()                  { return totalQcm; }
        public long getCompletedQcm()              { return completedQcm; }
        public BigDecimal getCourseGrade()         { return courseGrade; }
        public String getMention()                 { return mention; }
        public boolean isCourseCompleted()         { return courseCompleted; }
        public String getMotivationMessage()       { return motivationMessage; }
        public long getMissingAssignments()        { return totalAssignments - submittedAssignments; }
        public long getRemainingResources()        { return totalResources - consultedResources; }
        public long getRemainingQcm()              { return totalQcm - completedQcm; }
    }

    // ========================================
    // DTO ENSEIGNANT
    // ========================================

    public static class StudentProgressDTO {
        private final User student;
        private final int progressPercent;
        private final long submittedAssignments;
        private final long missingAssignments;
        private final long gradedAssignments;
        private final long lateAssignments;
        private final long consultedResources;
        private final long totalResources;
        private final BigDecimal courseGrade;
        private final boolean courseCompleted;
        private final String status;

        public StudentProgressDTO(User student, int progressPercent,
                                  long submittedAssignments, long missingAssignments,
                                  long gradedAssignments, long lateAssignments,
                                  long consultedResources, long totalResources,
                                  BigDecimal courseGrade, boolean courseCompleted, String status) {
            this.student = student;
            this.progressPercent = progressPercent;
            this.submittedAssignments = submittedAssignments;
            this.missingAssignments = missingAssignments;
            this.gradedAssignments = gradedAssignments;
            this.lateAssignments = lateAssignments;
            this.consultedResources = consultedResources;
            this.totalResources = totalResources;
            this.courseGrade = courseGrade;
            this.courseCompleted = courseCompleted;
            this.status = status;
        }

        public User getStudent()                   { return student; }
        public int getProgressPercent()            { return progressPercent; }
        public long getSubmittedAssignments()      { return submittedAssignments; }
        public long getMissingAssignments()        { return missingAssignments; }
        public long getGradedAssignments()         { return gradedAssignments; }
        public long getLateAssignments()           { return lateAssignments; }
        public long getConsultedResources()        { return consultedResources; }
        public long getTotalResources()            { return totalResources; }
        public BigDecimal getCourseGrade()         { return courseGrade; }
        public boolean isCourseCompleted()         { return courseCompleted; }
        public String getStatus()                  { return status; }
    }
}