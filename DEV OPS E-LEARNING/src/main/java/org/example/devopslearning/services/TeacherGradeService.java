package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.*;
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
    private final QcmTentativeRepository qcmTentativeRepository;
    private final QcmRepository qcmRepository;
    private final CoursClassRepository coursClassRepository;        // lien cours <-> classe
    private final InscriptionsClassRepository inscriptionsClassRepository; // étudiants d'une classe

    // ========================================
    // 1. VUE D'ENSEMBLE : COURS PAR CLASSE
    // ========================================

    /**
     * Retourne tous les cours du prof regroupés par classe académique.
     * Clé = AcademicClass (null = cours sans classe affectée).
     */
    public Map<AcademicClass, List<CourseGradeStats>> getCoursesGroupedByClass(User teacher) {
        List<Cours> courses = coursRepository.findByCreatedBy(teacher);

        Map<AcademicClass, List<CourseGradeStats>> result = new LinkedHashMap<>();

        for (Cours cours : courses) {
            CourseGradeStats stats = getCourseStats(cours);

            // Récupère les classes liées à ce cours via cours_classes
            List<CoursClass> coursClasses = coursClassRepository.findByClasseId(cours.getId());

            if (coursClasses.isEmpty()) {
                // Cours sans classe
                result.computeIfAbsent(null, k -> new ArrayList<>()).add(stats);
            } else {
                for (CoursClass cc : coursClasses) {
                    result.computeIfAbsent(cc.getClasse(), k -> new ArrayList<>()).add(stats);
                }
            }
        }

        return result;
    }

    /**
     * Vue plate : tous les cours avec stats (pour affichage liste simple)
     */
    public List<CourseGradeStats> getAllCoursesWithStats(User teacher) {
        return coursRepository.findByCreatedBy(teacher).stream()
                .map(this::getCourseStats)
                .collect(Collectors.toList());
    }

    // ========================================
    // 2. STATS D'UN COURS (devoirs + QCM)
    // ========================================

    public CourseGradeStats getCourseStats(Cours cours) {
        List<NotesCour> notes = notesCoursRepository.findByCours(cours);

        long totalAssignments = assignmentRepository.countByCourse(cours);
        long gradedAssignments = assignmentRepository.countGradedAssignmentsByCourse(cours.getId());

        // QCM du cours
        List<Qcm> qcms = qcmRepository.findByCoursId(cours.getId());
        long totalQcm = qcms.size();
        long totalQcmTentatives = qcms.stream()
                .mapToLong(q -> qcmTentativeRepository.countByQcmId(q.getId()))
                .sum();

        BigDecimal moyenneGenerale = BigDecimal.ZERO;
        if (!notes.isEmpty()) {
            List<BigDecimal> validNotes = notes.stream()
                    .filter(n -> n.getNoteFinale() != null)
                    .map(NotesCour::getNoteFinale)
                    .collect(Collectors.toList());
            if (!validNotes.isEmpty()) {
                BigDecimal sum = validNotes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                moyenneGenerale = sum.divide(BigDecimal.valueOf(validNotes.size()), 2, RoundingMode.HALF_UP);
            }
        }

        long totalStudents = notes.size();
        long studentsWithGrades = notes.stream().filter(n -> n.getNoteFinale() != null).count();

        Map<String, Long> mentionDistribution = notes.stream()
                .filter(n -> n.getMention() != null)
                .collect(Collectors.groupingBy(NotesCour::getMention, Collectors.counting()));

        return CourseGradeStats.builder()
                .cours(cours)
                .totalStudents(totalStudents)
                .studentsWithGrades(studentsWithGrades)
                .totalAssignments(totalAssignments)
                .gradedAssignments(gradedAssignments)
                .totalQcm(totalQcm)
                .totalQcmTentatives(totalQcmTentatives)
                .moyenneGenerale(moyenneGenerale)
                .mentionDistribution(mentionDistribution)
                .build();
    }

    // ========================================
    // 3. DÉTAILS D'UN COURS : DEVOIRS + QCM PAR ÉTUDIANT
    // ========================================

    /**
     * Retourne le détail complet des notes d'un cours par étudiant
     * (devoirs corrigés + résultats QCM)
     */
    public List<StudentGradeDetail> getCourseGradeDetails(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<NotesCour> notes = notesCoursRepository.findByCoursId(coursId);
        List<Assignment> assignments = assignmentRepository.findByCourseId(coursId);
        List<Qcm> qcms = qcmRepository.findByCoursId(coursId);

        return notes.stream().map(note -> {
            User student = note.getEtudiant();

            // Soumissions devoirs
            List<AssignmentSubmission> submissions = new ArrayList<>();
            for (Assignment a : assignments) {
                submissions.addAll(submissionRepository.findByAssignmentAndStudent(a, student));
            }

            // Tentatives QCM (seulement TERMINE)
            List<QcmTentative> tentatives = new ArrayList<>();
            for (Qcm q : qcms) {
                tentatives.addAll(
                        qcmTentativeRepository.findByQcmIdAndStatut(q.getId(), "TERMINE")
                                .stream()
                                .filter(t -> t.getEtudiant().getId().equals(student.getId()))
                                .collect(Collectors.toList())
                );
            }

            // Moyenne QCM de l'étudiant (meilleure tentative par QCM)
            BigDecimal moyenneQcm = calculerMoyenneQcm(tentatives, qcms);

            return StudentGradeDetail.builder()
                    .notesCour(note)
                    .student(student)
                    .noteFinale(note.getNoteFinale())
                    .mention(note.getMention())
                    .statut(note.getStatut())
                    .submissions(submissions)
                    .tentativesQcm(tentatives)
                    .totalAssignments(assignments.size())
                    .submittedAssignments(submissions.size())
                    .gradedAssignments((int) submissions.stream().filter(s -> s.getGrade() != null).count())
                    .totalQcm(qcms.size())
                    .tentativesQcmCount(tentatives.size())
                    .moyenneQcm(moyenneQcm)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Détails des devoirs d'un cours (vue onglet Devoirs)
     */
    public List<AssignmentGradeDetail> getAssignmentDetails(Long coursId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(coursId);

        return assignments.stream().map(a -> {
            List<AssignmentSubmission> allSubs = submissionRepository.findByAssignmentId(a.getId());
            List<AssignmentSubmission> graded = allSubs.stream()
                    .filter(s -> s.getGrade() != null).collect(Collectors.toList());

            BigDecimal moyenne = BigDecimal.ZERO;
            if (!graded.isEmpty()) {
                BigDecimal sum = graded.stream().map(AssignmentSubmission::getGrade)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                moyenne = sum.divide(BigDecimal.valueOf(graded.size()), 2, RoundingMode.HALF_UP);
            }

            return AssignmentGradeDetail.builder()
                    .assignment(a)
                    .totalSubmissions(allSubs.size())
                    .gradedSubmissions(graded.size())
                    .pendingSubmissions(allSubs.size() - graded.size())
                    .moyenne(moyenne)
                    .submissions(allSubs)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Détails des QCM d'un cours (vue onglet QCM)
     */
    public List<QcmGradeDetail> getQcmDetails(Long coursId) {
        List<Qcm> qcms = qcmRepository.findByCoursId(coursId);

        return qcms.stream().map(q -> {
            List<QcmTentative> tentatives = qcmTentativeRepository.findByQcmId(q.getId());
            List<QcmTentative> terminees = tentatives.stream()
                    .filter(t -> "TERMINE".equals(t.getStatut())).collect(Collectors.toList());

            BigDecimal moyennePct = BigDecimal.ZERO;
            if (!terminees.isEmpty()) {
                OptionalDouble avg = terminees.stream()
                        .filter(t -> t.getPourcentage() != null)
                        .mapToDouble(QcmTentative::getPourcentage)
                        .average();
                if (avg.isPresent()) {
                    moyennePct = BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP);
                }
            }

            long reussies = terminees.stream().filter(t -> Boolean.TRUE.equals(t.isReussie())).count();

            return QcmGradeDetail.builder()
                    .qcm(q)
                    .totalTentatives(tentatives.size())
                    .tentativesTerminees(terminees.size())
                    .moyennePourcentage(moyennePct)
                    .tentativesReussies(reussies)
                    .tentativesEchouees(terminees.size() - reussies)
                    .build();
        }).collect(Collectors.toList());
    }

    // ========================================
    // 4. CALCUL DES NOTES (devoirs + QCM)
    // ========================================

    @Transactional
    public void calculateAllGradesForCourse(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<Assignment> assignments = assignmentRepository.findByCourseId(coursId);
        List<Qcm> qcms = qcmRepository.findByCoursId(coursId);

        Set<User> students = new HashSet<>();
        for (Assignment a : assignments) {
            submissionRepository.findByAssignmentId(a.getId())
                    .forEach(s -> students.add(s.getStudent()));
        }
        for (Qcm q : qcms) {
            qcmTentativeRepository.findByQcmId(q.getId())
                    .forEach(t -> students.add(t.getEtudiant()));
        }

        for (User student : students) {
            calculateStudentGrade(cours, student, assignments, qcms);
        }
    }

    @Transactional
    public void calculateStudentGrade(Cours cours, User student,
                                      List<Assignment> assignments, List<Qcm> qcms) {
        List<BigDecimal> grades = new ArrayList<>();

        // Notes des devoirs (normalisées sur 20)
        for (Assignment a : assignments) {
            List<AssignmentSubmission> subs = submissionRepository.findByAssignmentAndStudent(a, student);
            if (!subs.isEmpty() && subs.get(0).getGrade() != null) {
                BigDecimal grade = subs.get(0).getGrade();
                BigDecimal normalized = grade.divide(a.getMaxGrade(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(20));
                grades.add(normalized);
            }
        }

        // Notes des QCM (meilleure tentative, normalisée sur 20)
        for (Qcm q : qcms) {
            List<QcmTentative> tentatives = qcmTentativeRepository
                    .findByQcmIdAndStatut(q.getId(), "TERMINE")
                    .stream()
                    .filter(t -> t.getEtudiant().getId().equals(student.getId()))
                    .collect(Collectors.toList());

            tentatives.stream()
                    .filter(t -> t.getScore() != null && t.getScoreMax() != null
                            && t.getScoreMax().compareTo(BigDecimal.ZERO) > 0)
                    .max(Comparator.comparing(QcmTentative::getScore))
                    .ifPresent(best -> {
                        BigDecimal normalized = best.getScore()
                                .divide(best.getScoreMax(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(20));
                        grades.add(normalized);
                    });
        }

        if (grades.isEmpty()) return;

        BigDecimal moyenne = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);

        String mention = determineMention(moyenne);
        String statut = moyenne.compareTo(BigDecimal.valueOf(10)) >= 0 ? "VALIDE" : "PROVISOIRE";

        Optional<NotesCour> existing = notesCoursRepository.findByCoursAndEtudiant(cours, student);
        NotesCour notesCour = existing.orElseGet(() -> {
            NotesCour n = new NotesCour();
            n.setCours(cours);
            n.setEtudiant(student);
            return n;
        });

        notesCour.setNoteFinale(moyenne);
        notesCour.setNoteMax(BigDecimal.valueOf(20));
        notesCour.setMention(mention);
        notesCour.setStatut(statut);
        notesCour.setDateCalcul(Instant.now());

        notesCoursRepository.save(notesCour);
    }

    // ========================================
    // 5. STATISTIQUES AVANCÉES
    // ========================================

    public Map<String, Long> getGradeDistribution(Long coursId) {
        return notesCoursRepository.findByCoursId(coursId).stream()
                .filter(n -> n.getNoteFinale() != null)
                .collect(Collectors.groupingBy(
                        n -> getGradeRange(n.getNoteFinale()), Collectors.counting()));
    }

    public BigDecimal getSuccessRate(Long coursId) {
        List<NotesCour> notes = notesCoursRepository.findByCoursId(coursId);
        if (notes.isEmpty()) return BigDecimal.ZERO;
        long passed = notes.stream().filter(n -> n.getNoteFinale() != null
                && n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) >= 0).count();
        return BigDecimal.valueOf(passed * 100L)
                .divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);
    }

    public List<NotesCour> getStudentsAtRisk(Long coursId) {
        return notesCoursRepository.findByCoursId(coursId).stream()
                .filter(n -> n.getNoteFinale() != null
                        && n.getNoteFinale().compareTo(BigDecimal.valueOf(10)) < 0)
                .collect(Collectors.toList());
    }

    public List<NotesCour> getTopStudents(Long coursId, int limit) {
        return notesCoursRepository.findByCoursId(coursId).stream()
                .filter(n -> n.getNoteFinale() != null)
                .sorted((a, b) -> b.getNoteFinale().compareTo(a.getNoteFinale()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Optional<NotesCour> getStudentGradeForCourse(Long coursId, Long studentId) {
        return notesCoursRepository.findByCoursIdAndEtudiantId(coursId, studentId);
    }

    // ========================================
    // 6. HELPERS PRIVÉS
    // ========================================

    private BigDecimal calculerMoyenneQcm(List<QcmTentative> tentatives, List<Qcm> qcms) {
        if (tentatives.isEmpty() || qcms.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> scores = tentatives.stream()
                .filter(t -> t.getPourcentage() != null)
                .map(t -> BigDecimal.valueOf(t.getPourcentage())
                        .divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP)) // % → /20
                .collect(Collectors.toList());
        if (scores.isEmpty()) return BigDecimal.ZERO;
        return scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private String determineMention(BigDecimal note) {
        if (note.compareTo(BigDecimal.valueOf(16)) >= 0) return "TB";
        if (note.compareTo(BigDecimal.valueOf(14)) >= 0) return "B";
        if (note.compareTo(BigDecimal.valueOf(12)) >= 0) return "AB";
        if (note.compareTo(BigDecimal.valueOf(10)) >= 0) return "P";
        return null;
    }

    private String getGradeRange(BigDecimal note) {
        if (note.compareTo(BigDecimal.valueOf(16)) >= 0) return "16-20";
        if (note.compareTo(BigDecimal.valueOf(14)) >= 0) return "14-16";
        if (note.compareTo(BigDecimal.valueOf(12)) >= 0) return "12-14";
        if (note.compareTo(BigDecimal.valueOf(10)) >= 0) return "10-12";
        if (note.compareTo(BigDecimal.valueOf(8)) >= 0) return "8-10";
        return "0-8";
    }

    // ========================================
    // 7. DTOs
    // ========================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseGradeStats {
        private Cours cours;
        private long totalStudents;
        private long studentsWithGrades;
        private long totalAssignments;
        private long gradedAssignments;
        private long totalQcm;
        private long totalQcmTentatives;
        private BigDecimal moyenneGenerale;
        private Map<String, Long> mentionDistribution;

        public double getGradedPercentage() {
            if (totalStudents == 0) return 0.0;
            return (double) studentsWithGrades / totalStudents * 100;
        }

        public double getAssignmentGradedPercentage() {
            if (totalAssignments == 0) return 0.0;
            return (double) gradedAssignments / totalAssignments * 100;
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StudentGradeDetail {
        private NotesCour notesCour;
        private User student;
        private BigDecimal noteFinale;
        private String mention;
        private String statut;
        private List<AssignmentSubmission> submissions;
        private List<QcmTentative> tentativesQcm;
        private int totalAssignments;
        private int submittedAssignments;
        private int gradedAssignments;
        private int totalQcm;
        private int tentativesQcmCount;
        private BigDecimal moyenneQcm;

        public double getSubmissionRate() {
            if (totalAssignments == 0) return 0.0;
            return (double) submittedAssignments / totalAssignments * 100;
        }

        public boolean isAtRisk() {
            return noteFinale != null && noteFinale.compareTo(BigDecimal.valueOf(10)) < 0;
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AssignmentGradeDetail {
        private Assignment assignment;
        private int totalSubmissions;
        private int gradedSubmissions;
        private int pendingSubmissions;
        private BigDecimal moyenne;
        private List<AssignmentSubmission> submissions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QcmGradeDetail {
        private Qcm qcm;
        private long totalTentatives;
        private long tentativesTerminees;
        private BigDecimal moyennePourcentage;
        private long tentativesReussies;
        private long tentativesEchouees;

        public double getTauxReussite() {
            if (tentativesTerminees == 0) return 0.0;
            return (double) tentativesReussies / tentativesTerminees * 100;
        }
    }
}
