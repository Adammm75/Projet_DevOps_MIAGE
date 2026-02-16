package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherGradeService {

    private final NotesCourRepository notesCoursRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CoursRepository coursRepository;

    // ========================================
    // 1. VUE D'ENSEMBLE DES COURS
    // ========================================

    /**
     * ✅ Récupère tous les cours d'un enseignant avec statistiques
     */
    public List<CourseGradeStats> getAllCoursesWithStats(User teacher) {
        List<Cours> courses = coursRepository.findByCreatedBy(teacher);

        return courses.stream()
                .map(this::getCourseStats)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Statistiques d'un cours
     */
    public CourseGradeStats getCourseStats(Cours cours) {
        // Récupérer toutes les notes du cours
        List<NotesCour> notes = notesCoursRepository.findByCours(cours);

        // Compter les devoirs
        long totalAssignments = assignmentRepository.countByCourse(cours);
        long gradedAssignments = assignmentRepository.countGradedAssignmentsByCourse(cours.getId());

        // Calculer moyenne générale
        BigDecimal moyenneGenerale = BigDecimal.ZERO;
        if (!notes.isEmpty()) {
            List<BigDecimal> validNotes = notes.stream()
                    .filter(n -> n.getNoteFinale() != null)
                    .map(NotesCour::getNoteFinale)
                    .collect(Collectors.toList());

            if (!validNotes.isEmpty()) {
                BigDecimal sum = validNotes.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                moyenneGenerale = sum.divide(
                        BigDecimal.valueOf(validNotes.size()),
                        2,
                        RoundingMode.HALF_UP
                );
            }
        }

        // Compter étudiants
        long totalStudents = notes.size();
        long studentsWithGrades = notes.stream()
                .filter(n -> n.getNoteFinale() != null)
                .count();

        // Répartition des mentions
        Map<String, Long> mentionDistribution = notes.stream()
                .filter(n -> n.getMention() != null)
                .collect(Collectors.groupingBy(NotesCour::getMention, Collectors.counting()));

        return CourseGradeStats.builder()
                .cours(cours)
                .totalStudents(totalStudents)
                .studentsWithGrades(studentsWithGrades)
                .totalAssignments(totalAssignments)
                .gradedAssignments(gradedAssignments)
                .moyenneGenerale(moyenneGenerale)
                .mentionDistribution(mentionDistribution)
                .build();
    }

    // ========================================
    // 2. DÉTAILS DES NOTES D'UN COURS
    // ========================================

    /**
     * ✅ Récupère toutes les notes d'un cours avec détails (CORRIGÉ LIGNE 112)
     */
    public List<StudentGradeDetail> getCourseGradeDetails(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<NotesCour> notes = notesCoursRepository.findByCoursId(coursId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(coursId);

        return notes.stream()
                .map(note -> {
                    // ✅ CORRIGÉ LIGNE 112 : findByAssignmentAndStudent retourne une List
                    List<AssignmentSubmission> submissions = new ArrayList<>();
                    for (Assignment assignment : assignments) {
                        List<AssignmentSubmission> studentSubmissions =
                                submissionRepository.findByAssignmentAndStudent(assignment, note.getEtudiant());
                        submissions.addAll(studentSubmissions);
                    }

                    return StudentGradeDetail.builder()
                            .notesCour(note)
                            .student(note.getEtudiant())
                            .noteFinale(note.getNoteFinale())
                            .mention(note.getMention())
                            .statut(note.getStatut())
                            .submissions(submissions)
                            .totalAssignments(assignments.size())
                            .submittedAssignments(submissions.size())
                            .gradedAssignments((int) submissions.stream()
                                    .filter(s -> s.getGrade() != null)
                                    .count())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ Récupère les notes d'un étudiant pour un cours
     */
    public Optional<NotesCour> getStudentGradeForCourse(Long coursId, Long studentId) {
        return notesCoursRepository.findByCoursIdAndEtudiantId(coursId, studentId);
    }

    // ========================================
    // 3. CALCUL ET RECALCUL DES NOTES
    // ========================================

    /**
     * ✅ Calcule ou recalcule toutes les notes d'un cours
     */
    @Transactional
    public void calculateAllGradesForCourse(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<Assignment> assignments = assignmentRepository.findByCourseId(coursId);

        if (assignments.isEmpty()) {
            throw new RuntimeException("Aucun devoir trouvé pour ce cours");
        }

        // Récupérer tous les étudiants qui ont soumis au moins un devoir
        Set<User> students = new HashSet<>();
        for (Assignment assignment : assignments) {
            List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignment.getId());
            submissions.forEach(s -> students.add(s.getStudent()));
        }

        // Calculer la note pour chaque étudiant
        for (User student : students) {
            calculateStudentGrade(cours, student, assignments);
        }
    }

    /**
     * ✅ Calcule la note d'un étudiant pour un cours (CORRIGÉ LIGNE 180)
     */
    @Transactional
    public void calculateStudentGrade(Cours cours, User student, List<Assignment> assignments) {
        // Récupérer toutes les notes des devoirs de l'étudiant
        List<BigDecimal> grades = new ArrayList<>();

        for (Assignment assignment : assignments) {
            // ✅ CORRIGÉ LIGNE 180 : findByAssignmentAndStudent retourne une List
            List<AssignmentSubmission> submissions =
                    submissionRepository.findByAssignmentAndStudent(assignment, student);

            // Prendre la première soumission si elle existe et a une note
            if (!submissions.isEmpty() && submissions.get(0).getGrade() != null) {
                AssignmentSubmission submission = submissions.get(0);

                // Normaliser la note sur 20
                BigDecimal grade = submission.getGrade();
                BigDecimal maxGrade = assignment.getMaxGrade();
                BigDecimal normalizedGrade = grade.divide(maxGrade, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(20));
                grades.add(normalizedGrade);
            }
        }

        if (grades.isEmpty()) {
            return; // Pas de notes à calculer
        }

        // Calculer la moyenne
        BigDecimal moyenne = grades.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);

        // Déterminer la mention
        String mention = determineMention(moyenne);

        // Déterminer le statut
        String statut = moyenne.compareTo(BigDecimal.valueOf(10)) >= 0 ? "VALIDE" : "PROVISOIRE";

        // Créer ou mettre à jour la note du cours
        Optional<NotesCour> existingNote = notesCoursRepository.findByCoursAndEtudiant(cours, student);

        NotesCour notesCour;
        if (existingNote.isPresent()) {
            notesCour = existingNote.get();
        } else {
            notesCour = new NotesCour();
            notesCour.setCours(cours);
            notesCour.setEtudiant(student);
        }

        notesCour.setNoteFinale(moyenne);
        notesCour.setNoteMax(BigDecimal.valueOf(20));
        notesCour.setMention(mention);
        notesCour.setStatut(statut);
        notesCour.setDateCalcul(Instant.now());

        notesCoursRepository.save(notesCour);
    }

    /**
     * ✅ Détermine la mention selon la note
     */
    private String determineMention(BigDecimal note) {
        if (note.compareTo(BigDecimal.valueOf(16)) >= 0) {
            return "TB"; // Très Bien
        } else if (note.compareTo(BigDecimal.valueOf(14)) >= 0) {
            return "B";  // Bien
        } else if (note.compareTo(BigDecimal.valueOf(12)) >= 0) {
            return "AB"; // Assez Bien
        } else if (note.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "P";  // Passable
        } else {
            return null; // Pas de mention si < 10
        }
    }

    // ========================================
    // 4. STATISTIQUES AVANCÉES
    // ========================================

    /**
     * ✅ Répartition des notes d'un cours
     */
    public Map<String, Long> getGradeDistribution(Long coursId) {
        List<NotesCour> notes = notesCoursRepository.findByCoursId(coursId);

        return notes.stream()
                .filter(n -> n.getNoteFinale() != null)
                .collect(Collectors.groupingBy(
                        n -> getGradeRange(n.getNoteFinale()),
                        Collectors.counting()
                ));
    }

    /**
     * ✅ Détermine la tranche de note
     */
    private String getGradeRange(BigDecimal note) {
        if (note.compareTo(BigDecimal.valueOf(16)) >= 0) return "16-20";
        if (note.compareTo(BigDecimal.valueOf(14)) >= 0) return "14-16";
        if (note.compareTo(BigDecimal.valueOf(12)) >= 0) return "12-14";
        if (note.compareTo(BigDecimal.valueOf(10)) >= 0) return "10-12";
        if (note.compareTo(BigDecimal.valueOf(8)) >= 0) return "8-10";
        return "0-8";
    }

    /**
     * ✅ Taux de réussite d'un cours
     */
    public BigDecimal getSuccessRate(Long coursId) {
        List<NotesCour> notes = notesCoursRepository.findByCoursId(coursId);

        long total = notes.size();
        if (total == 0) return BigDecimal.ZERO;

        long passed = notes.stream()
                .filter(n -> n.getNoteFinale() != null &&
                        n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) >= 0)
                .count();

        return BigDecimal.valueOf(passed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * ✅ Étudiants en difficulté (note < 10)
     */
    public List<NotesCour> getStudentsAtRisk(Long coursId) {
        return notesCoursRepository.findByCoursId(coursId).stream()
                .filter(n -> n.getNoteFinale() != null &&
                        n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) < 0)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Meilleurs étudiants (top N)
     */
    public List<NotesCour> getTopStudents(Long coursId, int limit) {
        return notesCoursRepository.findByCoursId(coursId).stream()
                .filter(n -> n.getNoteFinale() != null)
                .sorted((n1, n2) -> n2.getNoteFinale().compareTo(n1.getNoteFinale()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ========================================
    // 5. CLASSES DTO (DATA TRANSFER OBJECTS)
    // ========================================

    /**
     * ✅ DTO pour les statistiques d'un cours
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CourseGradeStats {
        private Cours cours;
        private long totalStudents;
        private long studentsWithGrades;
        private long totalAssignments;
        private long gradedAssignments;
        private BigDecimal moyenneGenerale;
        private Map<String, Long> mentionDistribution;

        /**
         * Calcule le pourcentage d'étudiants notés
         */
        public double getGradedPercentage() {
            if (totalStudents == 0) return 0.0;
            return (double) studentsWithGrades / totalStudents * 100;
        }

        /**
         * Calcule le pourcentage de devoirs corrigés
         */
        public double getAssignmentGradedPercentage() {
            if (totalAssignments == 0) return 0.0;
            return (double) gradedAssignments / totalAssignments * 100;
        }
    }

    /**
     * ✅ DTO pour les détails des notes d'un étudiant
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StudentGradeDetail {
        private NotesCour notesCour;
        private User student;
        private BigDecimal noteFinale;
        private String mention;
        private String statut;
        private List<AssignmentSubmission> submissions;
        private int totalAssignments;
        private int submittedAssignments;
        private int gradedAssignments;

        /**
         * Calcule le taux de soumission
         */
        public double getSubmissionRate() {
            if (totalAssignments == 0) return 0.0;
            return (double) submittedAssignments / totalAssignments * 100;
        }

        /**
         * Calcule le taux de correction
         */
        public double getGradedRate() {
            if (submittedAssignments == 0) return 0.0;
            return (double) gradedAssignments / submittedAssignments * 100;
        }

        /**
         * Vérifie si l'étudiant est en difficulté
         */
        public boolean isAtRisk() {
            return noteFinale != null &&
                    noteFinale.compareTo(BigDecimal.valueOf(10)) < 0;
        }
    }
}
