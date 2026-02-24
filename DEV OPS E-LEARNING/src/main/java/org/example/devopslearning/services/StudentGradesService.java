package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.repositories.NotesCourRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGradesService {

    private final NotesCourRepository notesCourRepository;

    public List<NotesCour> getAllGrades(Long studentId) {
        return notesCourRepository.findByEtudiantId(studentId);
    }

    public List<NotesCour> getGradesByCourse(Long studentId, Long courseId) {
        return notesCourRepository.findByEtudiantIdAndCoursId(studentId, courseId);
    }

    public Map<Long, List<NotesCour>> getGradesGroupedByCourse(Long studentId) {
        return getAllGrades(studentId).stream()
                .collect(Collectors.groupingBy(n -> n.getCours().getId()));
    }

    public Map<String, Object> calculateStatistics(Long studentId) {
        List<NotesCour> notes = getAllGrades(studentId);
        Map<String, Object> stats = new HashMap<>();

        if (notes.isEmpty()) {
            stats.put("averageGrade", BigDecimal.ZERO);
            stats.put("totalGrades", 0);
            stats.put("passedCourses", 0);
            stats.put("failedCourses", 0);
            return stats;
        }

        BigDecimal sum = notes.stream()
                .map(NotesCour::getNoteFinale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);

        long passed = notes.stream().filter(n -> n.getNoteFinale().compareTo(BigDecimal.TEN) >= 0).count();
        long failed = notes.size() - passed;

        stats.put("averageGrade", avg);
        stats.put("totalGrades", notes.size());
        stats.put("passedCourses", passed);
        stats.put("failedCourses", failed);
        stats.put("highestGrade", notes.stream().map(NotesCour::getNoteFinale).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        stats.put("lowestGrade", notes.stream().map(NotesCour::getNoteFinale).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

        return stats;
    }

    public Map<String, Object> getCourseStatistics(Long studentId, Long courseId) {
        List<NotesCour> notes = getGradesByCourse(studentId, courseId);
        Map<String, Object> stats = new HashMap<>();

        if (notes.isEmpty()) {
            stats.put("courseAverage", BigDecimal.ZERO);
            stats.put("totalNotes", 0);
            return stats;
        }

        BigDecimal sum = notes.stream().map(NotesCour::getNoteFinale).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);

        stats.put("courseAverage", avg);
        stats.put("totalNotes", notes.size());
        stats.put("isPassed", avg.compareTo(BigDecimal.TEN) >= 0);

        return stats;
    }

    public List<NotesCour> getGradesOrderedByDate(Long studentId) {
        return getAllGrades(studentId).stream()
                .sorted(Comparator.comparing(NotesCour::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getGradesEvolution(Long studentId) {
        List<NotesCour> notes = getGradesOrderedByDate(studentId);
        Map<String, Object> evolution = new HashMap<>();

        List<BigDecimal> grades = notes.stream().map(NotesCour::getNoteFinale).collect(Collectors.toList());
        List<String> dates = notes.stream()
                .map(n -> n.getCreatedAt().toString().substring(0, 10))
                .collect(Collectors.toList());

        evolution.put("grades", grades);
        evolution.put("dates", dates);

        return evolution;
    }

    public NotesCour getGradeById(Long gradeId) {
        return notesCourRepository.findById(gradeId).orElseThrow(() -> new RuntimeException("Note introuvable"));
    }
}