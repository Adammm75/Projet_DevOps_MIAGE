package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BulkEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final InscriptionsClassRepository inscriptionsClassRepository;
    private final CoursRepository coursRepository;
    private final AcademicClassRepository academicClassRepository;
    private final UserRepository userRepository;
    private final CourseTeacherRepository courseTeacherRepository;
    private final AuditService auditService;

    /**
     * Inscrit toute une classe à un cours
     */
    @Transactional
    public int enrollClassToCourse(Long courseId, Long classId, Long adminId) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        AcademicClass classe = academicClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        // Récupérer tous les étudiants de la classe
        List<InscriptionsClass> inscriptions = inscriptionsClassRepository.findByClasseId(classId);

        int enrolledCount = 0;

        for (InscriptionsClass inscription : inscriptions) {
            User student = inscription.getEtudiant();

            // Vérifier si l'étudiant n'est pas déjà inscrit
            if (!enrollmentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
                CourseEnrollment enrollment = new CourseEnrollment();
                // ✅ PAS BESOIN de setId() - auto-généré par @GeneratedValue
                enrollment.setCourse(cours);
                enrollment.setStudent(student);
                enrollment.setEnrolledAt(Instant.now());
                enrollment.setStatus("ACTIVE");

                enrollmentRepository.save(enrollment);
                enrolledCount++;
            }
        }

        // Audit
        auditService.log(
                adminId,
                "BULK_ENROLL_CLASS",
                "COURSE",
                courseId,
                "Inscription de " + enrolledCount + " étudiants de la classe " + classe.getNom()
        );

        return enrolledCount;
    }

    /**
     * Affecte un enseignant à un cours
     */
    @Transactional
    public void assignTeacherToCourse(Long courseId, Long teacherId, Long adminId) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));

        // Vérifier si déjà affecté
        if (courseTeacherRepository.existsByCourseIdAndTeacherId(courseId, teacherId)) {
            throw new RuntimeException("Cet enseignant est déjà affecté à ce cours");
        }

        // Créer l'affectation
        CourseTeacherId id = new CourseTeacherId();
        id.setCourseId(courseId);
        id.setTeacherId(teacherId);

        CourseTeacher courseTeacher = new CourseTeacher();
        courseTeacher.setId(id);
        courseTeacher.setCourse(cours);
        courseTeacher.setTeacher(teacher);

        courseTeacherRepository.save(courseTeacher);

        // Audit
        auditService.log(
                adminId,
                "ASSIGN_TEACHER",
                "COURSE",
                courseId,
                "Affectation de " + teacher.getFirstName() + " " + teacher.getLastName()
        );
    }

    /**
     * Retire un enseignant d'un cours
     */
    @Transactional
    public void removeTeacherFromCourse(Long courseId, Long teacherId, Long adminId) {
        // Créer l'ID composite pour la suppression
        CourseTeacherId id = new CourseTeacherId(courseId, teacherId);

        if (courseTeacherRepository.existsById(id)) {
            courseTeacherRepository.deleteById(id);

            // Audit
            User teacher = userRepository.findById(teacherId).orElse(null);
            auditService.log(
                    adminId,
                    "REMOVE_TEACHER",
                    "COURSE",
                    courseId,
                    teacher != null ? "Retrait de " + teacher.getFirstName() + " " + teacher.getLastName() : "Retrait enseignant"
            );
        } else {
            throw new RuntimeException("Cet enseignant n'est pas affecté à ce cours");
        }
    }

    /**
     * Désinscrit un étudiant d'un cours
     */
    @Transactional
    public void unenrollStudent(Long courseId, Long studentId, Long adminId) {
        // Trouver et supprimer l'inscription
        CourseEnrollment enrollment = enrollmentRepository
                .findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

        enrollmentRepository.delete(enrollment);

        // Audit
        User student = userRepository.findById(studentId).orElse(null);
        auditService.log(
                adminId,
                "UNENROLL_STUDENT",
                "COURSE",
                courseId,
                student != null ? "Désinscription de " + student.getFirstName() + " " + student.getLastName() : "Désinscription étudiant"
        );
    }
// ✅ AJOUTER CETTE MÉTHODE DANS BulkEnrollmentService.java

    /**
     * Désinscrit toute une classe d'un cours
     */
    @Transactional
    public int unenrollClassFromCourse(Long courseId, Long classId, Long adminId) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        AcademicClass classe = academicClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        // Récupérer tous les étudiants de la classe
        List<InscriptionsClass> inscriptions = inscriptionsClassRepository.findByClasseId(classId);

        int unenrolledCount = 0;

        for (InscriptionsClass inscription : inscriptions) {
            User student = inscription.getEtudiant();

            // Vérifier si l'étudiant est inscrit au cours
            Optional<CourseEnrollment> enrollment = enrollmentRepository
                    .findByCourseIdAndStudentId(courseId, student.getId());

            if (enrollment.isPresent()) {
                enrollmentRepository.delete(enrollment.get());
                unenrolledCount++;
            }
        }

        // Audit
        auditService.log(
                adminId,
                "BULK_UNENROLL_CLASS",
                "COURSE",
                courseId,
                "Désinscription de " + unenrolledCount + " étudiants de la classe " + classe.getNom()
        );

        return unenrolledCount;
    }

}