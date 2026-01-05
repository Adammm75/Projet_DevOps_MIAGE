package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.CourseCreateRequest;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.RessourceCours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AssignmentRepository;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursService {

    private final CoursRepository coursRepository;
    private final CourseResourceRepository ressourceCoursRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    // ========================================
    // MÉTHODES DE BASE (CRUD)
    // ========================================

    /**
     * ✅ Récupère tous les cours d'un enseignant
     */
    public List<Cours> getCoursesByTeacher(User teacher) {
        return coursRepository.findByCreatedBy(teacher);
    }

    /**
     * ✅ NOUVEAU : Alias pour getCoursesByTeacher (utilisé dans TeacherAssignmentController)
     */
    public List<Cours> findByTeacher(User teacher) {
        return getCoursesByTeacher(teacher);
    }

    /**
     * ✅ Liste tous les cours
     */
    public List<Cours> listAll() {
        return coursRepository.findAll();
    }

    /**
     * ✅ Récupère un cours par son ID
     */
    public Cours getById(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable avec l'ID: " + id));
    }

    /**
     * ✅ NOUVEAU : Alias pour getById (pour cohérence)
     */
    public Cours findById(Long id) {
        return getById(id);
    }

    /**
     * ✅ Crée un nouveau cours (enseignant)
     */
    @Transactional
    public Cours createCourse(CourseCreateRequest form, User teacher) {
        // Vérifier si le code existe déjà
        if (coursRepository.existsByCode(form.getCode())) {
            throw new RuntimeException("Un cours avec ce code existe déjà: " + form.getCode());
        }

        Cours cours = new Cours();
        cours.setCode(form.getCode());
        cours.setTitle(form.getTitle());
        cours.setDescription(form.getDescription());
        cours.setCreatedBy(teacher);
        cours.setCreatedAt(Instant.now());

        return coursRepository.save(cours);
    }

    /**
     * ✅ Met à jour un cours
     */
    @Transactional
    public Cours updateCourse(Long id, CourseCreateRequest form) {
        Cours cours = getById(id);

        // Vérifier si le code a changé et s'il n'existe pas déjà
        if (!cours.getCode().equals(form.getCode())) {
            if (coursRepository.existsByCode(form.getCode())) {
                throw new RuntimeException("Un cours avec ce code existe déjà: " + form.getCode());
            }
        }

        cours.setCode(form.getCode());
        cours.setTitle(form.getTitle());
        cours.setDescription(form.getDescription());
        cours.setUpdatedAt(Instant.now());

        return coursRepository.save(cours);
    }

    /**
     * ✅ Suppression en cascade d'un cours avec ses ressources et assignments
     */
    @Transactional
    public void deleteCourse(Long id) {
        Cours cours = getById(id);

        // 1️⃣ Supprimer toutes les ressources associées
        List<RessourceCours> resources = ressourceCoursRepository.findByCourse(cours);
        if (!resources.isEmpty()) {
            ressourceCoursRepository.deleteAll(resources);
        }

        // 2️⃣ Supprimer tous les assignments associés
        List<Assignment> assignments = assignmentRepository.findByCourseId(id);
        if (!assignments.isEmpty()) {
            assignmentRepository.deleteAll(assignments);
        }

        // 3️⃣ Supprimer le cours lui-même
        coursRepository.deleteById(id);
    }

    // ========================================
    // GESTION DES RESSOURCES
    // ========================================

    /**
     * ✅ Ajoute une ressource (fichier) à un cours
     */
    @Transactional
    public RessourceCours addResourceToCourse(Long courseId, String originalName, String fileUrl) {
        Cours cours = getById(courseId);

        RessourceCours r = new RessourceCours();
        r.setCourse(cours);

        // Déterminer le type de fichier
        String name = originalName.toLowerCase();
        if (name.endsWith(".pdf")) {
            r.setType("PDF");
        } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
            r.setType("WORD");
        } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
            r.setType("PPT");
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            r.setType("EXCEL");
        } else if (name.endsWith(".zip") || name.endsWith(".rar")) {
            r.setType("ARCHIVE");
        } else if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) {
            r.setType("IMAGE");
        } else if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov")) {
            r.setType("VIDEO");
        } else {
            r.setType("FILE");
        }

        r.setTitle(originalName);
        r.setUrl(fileUrl);
        r.setCreatedAt(Instant.now());

        return ressourceCoursRepository.save(r);
    }

    /**
     * ✅ Récupère toutes les ressources d'un cours
     */
    public List<RessourceCours> getResourcesByCourse(Long courseId) {
        Cours cours = getById(courseId);
        return ressourceCoursRepository.findByCourse(cours);
    }

    /**
     * ✅ Alias court pour getResourcesByCourse
     */
    public List<RessourceCours> getResources(Long courseId) {
        return getResourcesByCourse(courseId);
    }

    /**
     * ✅ Récupère une ressource par son ID
     */
    public RessourceCours getResourceById(Long id) {
        return ressourceCoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'ID: " + id));
    }

    /**
     * ✅ Récupère l'ID du cours d'une ressource
     */
    public Long getCourseIdByResource(Long resourceId) {
        RessourceCours resource = getResourceById(resourceId);
        return resource.getCourse() != null ? resource.getCourse().getId() : null;
    }

    /**
     * ✅ Récupère l'URL d'une ressource
     */
    public String getResourceUrl(Long resourceId) {
        RessourceCours resource = getResourceById(resourceId);
        return resource.getUrl();
    }

    /**
     * ✅ Met à jour le titre d'une ressource
     */
    @Transactional
    public void updateResourceTitle(Long resourceId, String newTitle) {
        RessourceCours resource = getResourceById(resourceId);
        resource.setTitle(newTitle);
        ressourceCoursRepository.save(resource);
    }

    /**
     * ✅ Met à jour une ressource complètement
     */
    @Transactional
    public void updateResource(Long resourceId, String title, String description, String url) {
        RessourceCours resource = getResourceById(resourceId);

        if (title != null && !title.trim().isEmpty()) {
            resource.setTitle(title);
        }
        if (description != null) {
            resource.setDescription(description);
        }
        if (url != null && !url.trim().isEmpty()) {
            resource.setUrl(url);
        }

        ressourceCoursRepository.save(resource);
    }

    /**
     * ✅ Supprime une ressource
     */
    @Transactional
    public void deleteResource(Long resourceId) {
        RessourceCours resource = getResourceById(resourceId);
        ressourceCoursRepository.delete(resource);
    }

    /**
     * ✅ Compte le nombre de ressources d'un cours
     */
    public long countResourcesByCourse(Long courseId) {
        Cours cours = getById(courseId);
        return ressourceCoursRepository.countByCourse(cours);
    }

    // ========================================
    // GESTION DES ASSIGNMENTS
    // ========================================

    /**
     * ✅ Récupère les assignments d'un cours
     */
    public List<Assignment> getAssignments(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    /**
     * ✅ NOUVEAU : Compte le nombre d'assignments d'un cours
     */
    public long countAssignmentsByCourse(Long courseId) {
        return assignmentRepository.countByCourseId(courseId);
    }

    // ========================================
    // MÉTHODES POUR ADMIN
    // ========================================

    /**
     * ✅ Récupère tous les cours triés par date de création
     */
    public List<Cours> getAllCoursesOrderedByDate() {
        return coursRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * ✅ Recherche de cours par titre ou code
     */
    public List<Cours> searchCourses(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllCoursesOrderedByDate();
        }
        return coursRepository.searchCourses(search.trim());
    }

    /**
     * ✅ Crée un cours avec un enseignant spécifique (pour admin)
     */
    @Transactional
    public Cours createCourseByAdmin(Cours cours, Long teacherId) {
        // Vérifier que le code n'existe pas déjà
        if (coursRepository.existsByCode(cours.getCode())) {
            throw new RuntimeException("Un cours avec ce code existe déjà: " + cours.getCode());
        }

        // Récupérer l'enseignant
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + teacherId));

        cours.setCreatedBy(teacher);
        cours.setCreatedAt(Instant.now());

        return coursRepository.save(cours);
    }

    /**
     * ✅ Met à jour un cours (pour admin)
     */
    @Transactional
    public Cours updateCourseByAdmin(Long id, Cours updatedCours) {
        Cours existing = getById(id);

        // Vérifier si le code a changé et s'il n'existe pas déjà
        if (!existing.getCode().equals(updatedCours.getCode())) {
            if (coursRepository.existsByCode(updatedCours.getCode())) {
                throw new RuntimeException("Un cours avec ce code existe déjà: " + updatedCours.getCode());
            }
        }

        existing.setCode(updatedCours.getCode());
        existing.setTitle(updatedCours.getTitle());
        existing.setDescription(updatedCours.getDescription());
        existing.setUpdatedAt(Instant.now());

        return coursRepository.save(existing);
    }

    /**
     * ✅ Compte le nombre total de cours
     */
    public long countAllCourses() {
        return coursRepository.count();
    }

    /**
     * ✅ Vérifie si un code de cours existe
     */
    public boolean courseCodeExists(String code) {
        return coursRepository.existsByCode(code);
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * ✅ NOUVEAU : Compte le nombre de cours d'un enseignant
     */
    public long countCoursesByTeacher(User teacher) {
        return coursRepository.countByCreatedBy(teacher);
    }

    /**
     * ✅ NOUVEAU : Récupère les cours actifs (avec des étudiants inscrits)
     * NOTE: Cette méthode nécessite une table d'inscription (enrollments)
     */
    public List<Cours> getActiveCoursesForTeacher(User teacher) {
        // Pour l'instant, retourne tous les cours du prof
        // À améliorer avec une véritable table d'inscriptions
        return getCoursesByTeacher(teacher);
    }

    /**
     * ✅ NOUVEAU : Vérifie si un enseignant est propriétaire d'un cours
     */
    public boolean isTeacherOwner(Long courseId, User teacher) {
        Cours cours = getById(courseId);
        return cours.getCreatedBy() != null &&
                cours.getCreatedBy().getId().equals(teacher.getId());
    }
}
