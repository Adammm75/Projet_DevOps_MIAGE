package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.example.devopslearning.services.AdminStructureService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/structure")
@RequiredArgsConstructor
public class AdminStructureController {

    private final AdminStructureService structureService;
    private final AcademicClassRepository classRepo;
    private final InscriptionsClassRepository inscriptionsRepo;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;
    private final FiliereRepository filiereRepository;
    private final ParcourRepository parcourRepository;

    // ========================================
    // PAGE PRINCIPALE
    // ========================================

    @GetMapping
    public String structureHome(Model model) {
        List<Filiere> filieres = structureService.getAllFilieres();
        List<Parcour> parcours = structureService.getAllParcours();
        List<AcademicClass> classes = structureService.getAllClasses();

        model.addAttribute("filieres", filieres);
        model.addAttribute("parcours", parcours);
        model.addAttribute("classes", classes);

        return "admin/structure/index";
    }

    // ========================================
    // CRUD CLASSES ⭐ NOUVEAU
    // ========================================

    /**
     * Formulaire de création de classe
     */
    @GetMapping("/classes/create")
    public String createClasseForm(Model model) {
        model.addAttribute("classe", new AcademicClass());
        model.addAttribute("parcoursList", parcourRepository.findAll());
        return "admin/structure/classe-create";
    }

    /**
     * Créer une nouvelle classe
     */
    @PostMapping("/classes/create")
    public String createClasse(
            @RequestParam String nom,
            @RequestParam(required = false) String code,
            @RequestParam Long parcoursId,
            @RequestParam String anneeUniversitaire,
            RedirectAttributes ra
    ) {
        try {
            structureService.createClasse(nom, code, parcoursId, anneeUniversitaire);
            ra.addFlashAttribute("success", "Classe créée avec succès");
            return "redirect:/admin/structure";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/structure/classes/create";
        }
    }

    /**
     * Formulaire d'édition de classe
     */
    @GetMapping("/classes/{id}/edit")
    public String editClasseForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            AcademicClass classe = structureService.getClasseById(id);
            model.addAttribute("classe", classe);
            model.addAttribute("parcoursList", parcourRepository.findAll());
            return "admin/structure/classe-edit";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Classe introuvable");
            return "redirect:/admin/structure";
        }
    }

    /**
     * Mettre à jour une classe
     */
    @PostMapping("/classes/{id}/edit")
    public String updateClasse(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String code,
            @RequestParam Long parcoursId,
            @RequestParam String anneeUniversitaire,
            RedirectAttributes ra
    ) {
        try {
            AcademicClass classe = structureService.getClasseById(id);
            Parcour parcours = parcourRepository.findById(parcoursId)
                    .orElseThrow(() -> new RuntimeException("Parcours introuvable"));

            classe.setNom(nom);
            classe.setCode(code);
            classe.setParcours(parcours);
            classe.setAnneeUniversitaire(anneeUniversitaire);

            classRepo.save(classe);

            ra.addFlashAttribute("success", "Classe modifiée avec succès");
            return "redirect:/admin/structure/classes/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/structure/classes/" + id + "/edit";
        }
    }

    // ========================================
    // DÉTAILS CLASSE
    // ========================================

    @GetMapping("/classes/{id}")
    public String viewClasseDetails(@PathVariable Long id, Model model) {
        AcademicClass classe = structureService.getClasseById(id);

        // Étudiants
        List<InscriptionsClass> inscriptions = inscriptionsRepo.findByClasseId(id);
        long nbEtudiants = inscriptions.stream()
                .filter(i -> "ACTIF".equals(i.getStatut()))
                .count();

        List<Long> inscritIds = inscriptions.stream()
                .map(i -> i.getEtudiant().getId())
                .toList();
        List<User> availableStudents = userRepository.findAll().stream()
                .filter(u -> u.hasRole("ROLE_STUDENT"))
                .filter(u -> !inscritIds.contains(u.getId()))
                .toList();

        // Enseignants
        List<TeacherClass> enseignantsClasse = structureService.getEnseignantsClasse(id);
        List<User> enseignantsDisponibles = structureService.getEnseignantsDisponibles(id);

        // Cours
        List<CoursClass> coursClasse = structureService.getCoursClasse(id);
        List<Cours> coursDisponibles = structureService.getCoursDisponibles(id);

        model.addAttribute("classe", classe);
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("nbEtudiants", nbEtudiants);
        model.addAttribute("availableStudents", availableStudents);
        model.addAttribute("enseignantsClasse", enseignantsClasse);
        model.addAttribute("enseignantsDisponibles", enseignantsDisponibles);
        model.addAttribute("coursClasse", coursClasse);
        model.addAttribute("coursDisponibles", coursDisponibles);

        return "admin/structure/classe-detail";
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClasse(@PathVariable Long id, RedirectAttributes ra) {
        try {
            classRepo.deleteById(id);
            ra.addFlashAttribute("success", "Classe supprimée avec succès");
            return "redirect:/admin/structure";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/structure/classes/" + id;
        }
    }

    // ========================================
    // GESTION ENSEIGNANTS
    // ========================================

    @PostMapping("/classes/{classeId}/teachers/assign")
    public String assignTeacher(@PathVariable Long classeId, @RequestParam Long teacherId, RedirectAttributes ra) {
        try {
            structureService.affecterEnseignant(classeId, teacherId);
            ra.addFlashAttribute("success", "Enseignant affecté avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/teachers/assign-multiple")
    public String assignMultipleTeachers(@PathVariable Long classeId, @RequestParam(required = false) List<Long> teacherIds, RedirectAttributes ra) {
        try {
            if (teacherIds == null || teacherIds.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner au moins un enseignant");
            } else {
                structureService.affecterEnseignants(classeId, teacherIds);
                ra.addFlashAttribute("success", teacherIds.size() + " enseignant(s) affecté(s)");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/teachers/{teacherId}/remove")
    public String removeTeacher(@PathVariable Long classeId, @PathVariable Long teacherId, RedirectAttributes ra) {
        try {
            structureService.retirerEnseignant(classeId, teacherId);
            ra.addFlashAttribute("success", "Enseignant retiré");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    // ========================================
    // GESTION COURS
    // ========================================

    @PostMapping("/classes/{classeId}/courses/assign")
    public String assignCourse(@PathVariable Long classeId, @RequestParam Long coursId, RedirectAttributes ra) {
        try {
            structureService.affecterCours(classeId, coursId);
            ra.addFlashAttribute("success", "Cours affecté avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/courses/assign-multiple")
    public String assignMultipleCourses(@PathVariable Long classeId, @RequestParam(required = false) List<Long> coursIds, RedirectAttributes ra) {
        try {
            if (coursIds == null || coursIds.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner au moins un cours");
            } else {
                structureService.affecterCours(classeId, coursIds);
                ra.addFlashAttribute("success", coursIds.size() + " cours affecté(s)");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/courses/{coursId}/remove")
    public String removeCourse(@PathVariable Long classeId, @PathVariable Long coursId, RedirectAttributes ra) {
        try {
            structureService.retirerCours(classeId, coursId);
            ra.addFlashAttribute("success", "Cours retiré");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    // ========================================
    // GESTION ÉTUDIANTS
    // ========================================

    @PostMapping("/classes/{id}/students/enroll")
    public String enrollStudent(@PathVariable Long id, @RequestParam Long etudiantId, RedirectAttributes ra) {
        try {
            User student = userRepository.findById(etudiantId)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
            structureService.inscrireEtudiant(id, student);
            ra.addFlashAttribute("success", "Étudiant inscrit avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + id;
    }

    @PostMapping("/classes/{id}/students/enroll-multiple")
    public String enrollMultipleStudents(@PathVariable Long id, @RequestParam(required = false) List<Long> etudiantIds, RedirectAttributes ra) {
        try {
            if (etudiantIds == null || etudiantIds.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner au moins un étudiant");
            } else {
                int count = 0;
                for (Long etudiantId : etudiantIds) {
                    try {
                        User student = userRepository.findById(etudiantId)
                                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
                        structureService.inscrireEtudiant(id, student);
                        count++;
                    } catch (Exception e) {
                        // Continue
                    }
                }
                ra.addFlashAttribute("success", count + " étudiant(s) inscrit(s)");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + id;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/unenroll")
    public String unenrollStudent(@PathVariable Long classeId, @PathVariable Long etudiantId, RedirectAttributes ra) {
        try {
            InscriptionsClass inscription = inscriptionsRepo.findByClasseIdAndEtudiantId(classeId, etudiantId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
            inscription.setStatut("INACTIF");
            inscriptionsRepo.save(inscription);
            ra.addFlashAttribute("success", "Étudiant retiré");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/reactivate")
    public String reactivateStudent(@PathVariable Long classeId, @PathVariable Long etudiantId, RedirectAttributes ra) {
        try {
            InscriptionsClass inscription = inscriptionsRepo.findByClasseIdAndEtudiantId(classeId, etudiantId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
            inscription.setStatut("ACTIF");
            inscriptionsRepo.save(inscription);
            ra.addFlashAttribute("success", "Étudiant réactivé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/delete")
    public String deleteStudentEnrollment(@PathVariable Long classeId, @PathVariable Long etudiantId, RedirectAttributes ra) {
        try {
            InscriptionsClass inscription = inscriptionsRepo.findByClasseIdAndEtudiantId(classeId, etudiantId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
            inscriptionsRepo.delete(inscription);
            ra.addFlashAttribute("success", "Inscription supprimée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }
}