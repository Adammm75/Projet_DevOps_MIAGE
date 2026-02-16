package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AssignmentRepository;
import org.example.devopslearning.repositories.AssignmentSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final S3StorageService s3StorageService;

    /**
     * Liste tous les devoirs d'un cours
     */
    public List<Assignment> listByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    /**
     * Liste toutes les soumissions d'un étudiant
     */
    public List<AssignmentSubmission> submissionsForStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    /**
     * Liste toutes les soumissions pour un devoir
     */
    public List<AssignmentSubmission> submissionsForAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /**
     * Soumet un devoir avec un fichier
     */
    public AssignmentSubmission submitAssignment(Long assignmentId, User student, MultipartFile file) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        // Upload du fichier sur S3
        String fileUrl;
        try {
            fileUrl = s3StorageService.uploadFileInFolder(file, "assignments");
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier", e);
        }

        // Création de la soumission
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(Instant.now());

        return submissionRepository.save(submission);
    }

    /**
     * Récupère l'ID du cours associé à un devoir
     */
    public Long getCourseId(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Devoir introuvable"));

        return assignment.getCourse().getId();
    }
}