package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository            assignmentRepository;
    private final AssignmentSubmissionRepository  submissionRepository;
    private final S3StorageService                s3StorageService;
    private final AssignmentClassRepository       assignmentClassRepository;
    private final AcademicClassRepository         academicClassRepository;
    private final TeacherClassRepository          teacherClassRepository;
    private final BadgeEngineService              badgeEngineService;

    public List<Assignment> listByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    public List<AssignmentSubmission> submissionsForStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public List<AssignmentSubmission> submissionsForAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public AssignmentSubmission submitAssignment(Long assignmentId, User student, MultipartFile file) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        String fileUrl;
        try {
            fileUrl = s3StorageService.uploadFileInFolder(file, "assignments");
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier", e);
        }

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(Instant.now());

        AssignmentSubmission saved = submissionRepository.save(submission);

        // ✅ Badge Engine : points pour la remise du devoir
        Long courseId = assignment.getCourse().getId();
        badgeEngineService.onAssignmentSubmitted(student.getId(), courseId, assignmentId);

        return saved;
    }

    public AssignmentSubmission gradeSubmission(Long submissionId, double grade, String feedback) {

        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Soumission introuvable"));

        submission.setGrade(java.math.BigDecimal.valueOf(grade));
        submission.setFeedback(feedback);
        submission.setGradedAt(Instant.now());

        AssignmentSubmission saved = submissionRepository.save(submission);

        // ✅ Badge Engine : points bonus si note >= 14
        Long studentId = submission.getStudent().getId();
        Long courseId  = submission.getAssignment().getCourse().getId();
        badgeEngineService.onAssignmentGraded(studentId, courseId, submissionId, grade);

        return saved;
    }

    public Long getCourseId(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));
        return assignment.getCourse().getId();
    }

    public AssignmentClass affecterDevoirAClasse(Long assignmentId, Long classeId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        AcademicClass classe = academicClassRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        if (assignmentClassRepository.existsByAssignmentIdAndClasseId(assignmentId, classeId)) {
            throw new RuntimeException("Ce devoir est déjà affecté à cette classe");
        }

        AssignmentClass assignmentClass = new AssignmentClass();
        assignmentClass.setAssignment(assignment);
        assignmentClass.setClasse(classe);
        assignmentClass.setDateAffectation(Instant.now());

        return assignmentClassRepository.save(assignmentClass);
    }

    public void affecterDevoirAClasses(Long assignmentId, List<Long> classeIds) {
        if (classeIds == null || classeIds.isEmpty()) return;

        for (Long classeId : classeIds) {
            try {
                affecterDevoirAClasse(assignmentId, classeId);
            } catch (RuntimeException e) {
                if (!e.getMessage().contains("déjà affecté")) throw e;
            }
        }
    }

    @Transactional
    public void retirerDevoirDeClasse(Long assignmentId, Long classeId) {
        assignmentClassRepository.deleteByAssignmentIdAndClasseId(assignmentId, classeId);
    }

    public List<AssignmentClass> getClassesByAssignment(Long assignmentId) {
        return assignmentClassRepository.findByAssignmentId(assignmentId);
    }

    public List<AcademicClass> getClassesDisponibles(Long assignmentId, Long teacherId) {
        List<Long> assignedClasseIds = assignmentClassRepository.findClasseIdsByAssignmentId(assignmentId);

        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacherId(teacherId);

        return teacherClasses.stream()
                .map(TeacherClass::getClasse)
                .filter(c -> !assignedClasseIds.contains(c.getId()))
                .toList();
    }

    @Transactional
    public void updateClassesForAssignment(Long assignmentId, List<Long> newClasseIds) {
        List<Long> currentIds = assignmentClassRepository.findClasseIdsByAssignmentId(assignmentId);

        for (Long currentId : currentIds) {
            if (newClasseIds == null || !newClasseIds.contains(currentId)) {
                retirerDevoirDeClasse(assignmentId, currentId);
            }
        }

        if (newClasseIds != null) {
            affecterDevoirAClasses(assignmentId, newClasseIds);
        }
    }
}