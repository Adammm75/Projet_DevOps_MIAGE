package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des commentaires pédagogiques.
 * Commentaires internes enseignant → visible enseignants + admin.
 *
 * Innovation : tags prédéfinis avec sentiment (positif / neutre / négatif),
 * épinglage d'un commentaire, visibilité configurable.
 */
@Service
@RequiredArgsConstructor
public class TeacherCommentService {

    private final TeacherCommentRepository commentRepository;
    private final UserRepository           userRepository;
    private final CoursRepository          coursRepository;

    // Tags prédéfinis avec catégorie sémantique
    public enum CommentTag {
        // Positifs
        PARTICIPATION_ACTIVE("Participation active",      "success"),
        PROGRES_NOTABLE("Progrès notable",                "success"),
        SERIEUX("Étudiant sérieux",                       "success"),
        TRAVAIL_SOIGNE("Travail soigné",                  "success"),
        ESPRIT_EQUIPE("Esprit d'équipe",                  "success"),
        // Neutres
        EN_PROGRESSION("En progression",                  "info"),
        A_ENCOURAGER("À encourager",                      "info"),
        DISCRET("Discret mais présent",                   "info"),
        // Négatifs / attention
        MANQUE_REGULARITE("Manque de régularité",         "warning"),
        SOUVENT_EN_RETARD("Souvent en retard",            "warning"),
        DIFFICULTES("Difficultés à suivre",               "warning"),
        ABSENT("Absences répétées",                       "danger"),
        PERSONNALISE("Personnalisé",                      "secondary");

        private final String label;
        private final String colorClass;

        CommentTag(String label, String colorClass) {
            this.label = label;
            this.colorClass = colorClass;
        }

        public String getLabel()      { return label; }
        public String getColorClass() { return colorClass; }
    }

    // ========================================
    // LECTURE
    // ========================================

    public List<TeacherComment> getCommentsForStudent(Long studentId) {
        return commentRepository.findByStudentIdOrdered(studentId);
    }

    public List<TeacherComment> getVisibleCommentsForStudent(Long studentId) {
        return commentRepository.findVisibleByStudentId(studentId);
    }

    public List<TeacherComment> getCommentsForStudentInCourse(Long studentId, Long courseId) {
        return commentRepository.findByStudentAndCourse(studentId, courseId);
    }

    public List<TeacherComment> getAllComments() {
        return commentRepository.findAllOrdered();
    }

    public TeacherComment getById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));
    }

    public long countForStudent(Long studentId) {
        return commentRepository.countByStudentId(studentId);
    }

    public CommentTag[] getAllTags() {
        return CommentTag.values();
    }

    // ========================================
    // ÉCRITURE
    // ========================================

    @Transactional
    public TeacherComment addComment(Long teacherId, Long studentId, Long courseId,
                                     String content, String tag, String visibility) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        TeacherComment comment = new TeacherComment();
        comment.setTeacher(teacher);
        comment.setStudent(student);
        comment.setCourse(course);
        comment.setContent(content.trim());
        comment.setTag(tag != null && !tag.isBlank() ? tag.trim() : null);
        comment.setVisibility(visibility != null ? visibility : "TEACHER_ADMIN");
        comment.setIsPinned(false);

        return commentRepository.save(comment);
    }

    @Transactional
    public TeacherComment updateComment(Long commentId, Long teacherId,
                                        String content, String tag, String visibility) {
        TeacherComment comment = getById(commentId);
        // Seul l'auteur peut modifier
        if (!comment.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Non autorisé à modifier ce commentaire");
        }
        comment.setContent(content.trim());
        comment.setTag(tag != null && !tag.isBlank() ? tag.trim() : null);
        comment.setVisibility(visibility != null ? visibility : "TEACHER_ADMIN");
        return commentRepository.save(comment);
    }

    @Transactional
    public void togglePin(Long commentId, Long teacherId) {
        TeacherComment comment = getById(commentId);
        if (!comment.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Non autorisé");
        }
        comment.setIsPinned(!Boolean.TRUE.equals(comment.getIsPinned()));
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId, boolean isAdmin) {
        TeacherComment comment = getById(commentId);
        if (!isAdmin && !comment.getTeacher().getId().equals(requesterId)) {
            throw new RuntimeException("Non autorisé à supprimer ce commentaire");
        }
        commentRepository.delete(comment);
    }

    // Résoudre le label d'un tag depuis son nom enum
    public String getTagLabel(String tagName) {
        if (tagName == null) return null;
        try {
            return CommentTag.valueOf(tagName).getLabel();
        } catch (IllegalArgumentException e) {
            return tagName; // tag libre
        }
    }

    public String getTagColor(String tagName) {
        if (tagName == null) return "secondary";
        try {
            return CommentTag.valueOf(tagName).getColorClass();
        } catch (IllegalArgumentException e) {
            return "secondary";
        }
    }
}