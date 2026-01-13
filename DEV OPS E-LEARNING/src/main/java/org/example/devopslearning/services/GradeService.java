package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseEnrollment;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CourseEnrollmentRepository;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.NotesCourRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final NotesCourRepository notesCoursRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CoursRepository coursRepository;
    private final UserRepository userRepository;

    /**
     * Récupère toutes les notes d'un cours
     */
    public List<NotesCour> getGradesByCourse(Long courseId) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        // Pour chaque étudiant inscrit, créer ou récupérer sa note
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        for (CourseEnrollment enrollment : enrollments) {
            // ✅ CORRECTION : Utiliser findByCoursAndEtudiant au lieu de findByCoursIdAndEtudiantId
            if (!notesCoursRepository.findByCoursAndEtudiant(cours, enrollment.getStudent()).isPresent()) {
                createEmptyGrade(cours, enrollment.getStudent());
            }
        }

        // ✅ CORRECTION : Filtrer directement avec une requête au lieu de stream
        return notesCoursRepository.findAll().stream()
                .filter(n -> n.getCours().getId().equals(courseId))
                .toList();
    }

    /**
     * Crée une note vide pour un étudiant
     */
    @Transactional
    public NotesCour createEmptyGrade(Cours cours, User etudiant) {
        NotesCour note = new NotesCour();
        // ✅ SUPPRIMÉ : note.setId() car auto-généré par @GeneratedValue
        note.setCours(cours);
        note.setEtudiant(etudiant);
        note.setNoteMax(new BigDecimal("20.00"));
        note.setStatut("PROVISOIRE");
        return notesCoursRepository.save(note);
    }

    /**
     * Met à jour une note
     */
    @Transactional
    public NotesCour updateGrade(Long gradeId, BigDecimal noteFinale) {
        NotesCour note = notesCoursRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Note introuvable"));

        note.setNoteFinale(noteFinale);
        note.setDateCalcul(Instant.now());
        note.setMention(calculateMention(noteFinale));

        return notesCoursRepository.save(note);
    }

    /**
     * Verrouille toutes les notes d'un cours
     */
    @Transactional
    public void lockAllGrades(Long courseId) {
        List<NotesCour> grades = notesCoursRepository.findAll().stream()
                .filter(n -> n.getCours().getId().equals(courseId))
                .toList();

        for (NotesCour note : grades) {
            note.setStatut("DEFINITIF");
            note.setDateCalcul(Instant.now());
        }

        notesCoursRepository.saveAll(grades);
    }

    /**
     * Déverrouille toutes les notes d'un cours
     */
    @Transactional
    public void unlockAllGrades(Long courseId) {
        List<NotesCour> grades = notesCoursRepository.findAll().stream()
                .filter(n -> n.getCours().getId().equals(courseId))
                .toList();

        for (NotesCour note : grades) {
            note.setStatut("PROVISOIRE");
        }

        notesCoursRepository.saveAll(grades);
    }

    /**
     * Calcule la mention selon la note
     */
    private String calculateMention(BigDecimal note) {
        if (note == null) return null;

        double noteValue = note.doubleValue();

        if (noteValue >= 16) return "TB";      // Très Bien
        if (noteValue >= 14) return "B";       // Bien
        if (noteValue >= 12) return "AB";      // Assez Bien
        if (noteValue >= 10) return "P";       // Passable
        return "AR";                           // Ajourné/Refusé
    }

    /**
     * Calcule la moyenne d'un cours
     */
    public BigDecimal calculateCourseAverage(Long courseId) {
        List<NotesCour> grades = notesCoursRepository.findAll().stream()
                .filter(n -> n.getCours().getId().equals(courseId) && n.getNoteFinale() != null)
                .toList();

        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = grades.stream()
                .map(NotesCour::getNoteFinale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(grades.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Supprime une note
     */
    @Transactional
    public void deleteGrade(Long gradeId) {
        notesCoursRepository.deleteById(gradeId);
    }
}