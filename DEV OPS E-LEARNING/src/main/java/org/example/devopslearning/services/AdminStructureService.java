package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStructureService {

    private final FiliereRepository filiereRepo;
    private final ParcourRepository parcourRepo;
    private final AcademicClassRepository classRepo;
    private final InscriptionsClassRepository inscriptionsRepo;
    private final NiveauxEtudeRepository niveauxEtudeRepository;
    private final TeacherClassRepository teacherClassRepo;
    private final CoursClassRepository coursClassRepo;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;

    // ==================== FILIÈRES ====================

    public Filiere createFiliere(Filiere f) {
        return filiereRepo.save(f);
    }

    public List<Filiere> getAllFilieres() {
        return filiereRepo.findAll();
    }

    // ==================== PARCOURS ====================

    public Parcour createParcours(String nom, String description, Long filiereId, Long niveauId) {
        Filiere filiere = filiereRepo.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'ID : " + filiereId));

        NiveauxEtude niveau = niveauxEtudeRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé avec l'ID : " + niveauId));

        Parcour parcour = new Parcour();
        parcour.setNom(nom);
        parcour.setDescription(description);
        parcour.setFiliere(filiere);
        parcour.setNiveau(niveau);

        return parcourRepo.save(parcour);
    }

    public List<Parcour> getAllParcours() {
        return parcourRepo.findAll();
    }

    // ==================== CLASSES ====================

    /**
     * Créer une nouvelle classe
     */
    public AcademicClass createClasse(String nom, String code, Long parcoursId, String anneeUniversitaire) {
        Parcour parcours = parcourRepo.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé avec l'ID : " + parcoursId));

        AcademicClass classe = new AcademicClass();
        classe.setNom(nom);
        classe.setCode(code);
        classe.setParcours(parcours);
        classe.setAnneeUniversitaire(anneeUniversitaire);

        return classRepo.save(classe);
    }

    public List<AcademicClass> getAllClasses() {
        return classRepo.findAll();
    }

    public AcademicClass getClasseById(Long id) {
        return classRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID : " + id));
    }

    // ==================== INSCRIPTIONS ÉTUDIANTS ====================

    public void inscrireEtudiant(Long classeId, User etudiant) {
        AcademicClass classe = classRepo.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID : " + classeId));

        InscriptionsClass ic = new InscriptionsClass();
        ic.setClasse(classe);
        ic.setEtudiant(etudiant);

        inscriptionsRepo.save(ic);
    }

    // ==================== GESTION ENSEIGNANTS ====================

    /**
     * Affecte un enseignant à une classe
     */
    public void affecterEnseignant(Long classeId, Long teacherId) {
        AcademicClass classe = getClasseById(classeId);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID : " + teacherId));

        // Vérifier que c'est bien un enseignant
        if (!teacher.hasRole("ROLE_TEACHER") && !teacher.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("L'utilisateur n'est pas un enseignant");
        }

        // Vérifier si déjà affecté
        if (teacherClassRepo.existsByTeacherIdAndClasseId(teacherId, classeId)) {
            throw new RuntimeException("Cet enseignant est déjà affecté à cette classe");
        }

        TeacherClass tc = new TeacherClass();
        tc.setTeacher(teacher);
        tc.setClasse(classe);

        teacherClassRepo.save(tc);
    }

    /**
     * Affecte plusieurs enseignants à une classe
     */
    public void affecterEnseignants(Long classeId, List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return;
        }

        for (Long teacherId : teacherIds) {
            try {
                affecterEnseignant(classeId, teacherId);
            } catch (RuntimeException e) {
                // Si déjà affecté, continuer
                if (!e.getMessage().contains("déjà affecté")) {
                    throw e;
                }
            }
        }
    }

    /**
     * Retire un enseignant d'une classe
     */
    public void retirerEnseignant(Long classeId, Long teacherId) {
        teacherClassRepo.deleteByTeacherIdAndClasseId(teacherId, classeId);
    }

    /**
     * Récupère tous les enseignants d'une classe
     */
    public List<TeacherClass> getEnseignantsClasse(Long classeId) {
        return teacherClassRepo.findByClasseId(classeId);
    }

    /**
     * Récupère les enseignants disponibles (non affectés à la classe)
     */
    public List<User> getEnseignantsDisponibles(Long classeId) {
        List<Long> assignedTeacherIds = teacherClassRepo.findTeacherIdsByClasseId(classeId);

        return userRepository.findAll().stream()
                .filter(u -> u.hasRole("ROLE_TEACHER") || u.hasRole("ROLE_ADMIN"))
                .filter(u -> !assignedTeacherIds.contains(u.getId()))
                .toList();
    }

    // ==================== GESTION COURS ====================

    /**
     * Affecte un cours à une classe
     */
    public void affecterCours(Long classeId, Long coursId) {
        AcademicClass classe = getClasseById(classeId);
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'ID : " + coursId));

        // Vérifier si déjà affecté
        if (coursClassRepo.existsByCoursIdAndClasseId(coursId, classeId)) {
            throw new RuntimeException("Ce cours est déjà affecté à cette classe");
        }

        CoursClassId id = new CoursClassId();
        id.setCoursId(coursId);
        id.setClasseId(classeId);

        CoursClass cc = new CoursClass();
        cc.setId(id);
        cc.setCours(cours);
        cc.setClasse(classe);

        coursClassRepo.save(cc);
    }

    /**
     * Affecte plusieurs cours à une classe
     */
    public void affecterCours(Long classeId, List<Long> coursIds) {
        if (coursIds == null || coursIds.isEmpty()) {
            return;
        }

        for (Long coursId : coursIds) {
            try {
                affecterCours(classeId, coursId);
            } catch (RuntimeException e) {
                // Si déjà affecté, continuer
                if (!e.getMessage().contains("déjà affecté")) {
                    throw e;
                }
            }
        }
    }

    /**
     * Retire un cours d'une classe
     */
    public void retirerCours(Long classeId, Long coursId) {
        CoursClassId id = new CoursClassId();
        id.setCoursId(coursId);
        id.setClasseId(classeId);

        coursClassRepo.deleteById(id);
    }

    /**
     * Récupère tous les cours d'une classe
     */
    public List<CoursClass> getCoursClasse(Long classeId) {
        return coursClassRepo.findByClasseId(classeId);
    }

    /**
     * Récupère les cours disponibles (non affectés à la classe)
     */
    public List<Cours> getCoursDisponibles(Long classeId) {
        List<CoursClass> assignedCours = coursClassRepo.findByClasseId(classeId);
        List<Long> assignedCoursIds = assignedCours.stream()
                .map(cc -> cc.getCours().getId())
                .toList();

        return coursRepository.findAll().stream()
                .filter(c -> !assignedCoursIds.contains(c.getId()))
                .toList();
    }

    // ==================== MISE À JOUR GLOBALE ====================

    /**
     * Met à jour l'affectation complète des enseignants d'une classe
     */
    @Transactional
    public void updateTeachersForClasse(Long classeId, List<Long> newTeacherIds) {
        // Récupérer les IDs actuels
        List<Long> currentIds = teacherClassRepo.findTeacherIdsByClasseId(classeId);

        // Retirer ceux qui ne sont plus sélectionnés
        for (Long currentId : currentIds) {
            if (newTeacherIds == null || !newTeacherIds.contains(currentId)) {
                retirerEnseignant(classeId, currentId);
            }
        }

        // Ajouter les nouveaux
        if (newTeacherIds != null) {
            affecterEnseignants(classeId, newTeacherIds);
        }
    }

    /**
     * Met à jour l'affectation complète des cours d'une classe
     */
    @Transactional
    public void updateCoursForClasse(Long classeId, List<Long> newCoursIds) {
        // Récupérer les IDs actuels
        List<CoursClass> currentCours = coursClassRepo.findByClasseId(classeId);
        List<Long> currentIds = currentCours.stream()
                .map(cc -> cc.getCours().getId())
                .toList();

        // Retirer ceux qui ne sont plus sélectionnés
        for (Long currentId : currentIds) {
            if (newCoursIds == null || !newCoursIds.contains(currentId)) {
                retirerCours(classeId, currentId);
            }
        }

        // Ajouter les nouveaux
        if (newCoursIds != null) {
            affecterCours(classeId, newCoursIds);
        }
    }
}