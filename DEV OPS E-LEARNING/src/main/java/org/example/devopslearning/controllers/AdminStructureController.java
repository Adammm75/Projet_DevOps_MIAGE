package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.AcademicClassRepository;
import org.example.devopslearning.repositories.FiliereRepository;
import org.example.devopslearning.repositories.NiveauxEtudeRepository;
import org.example.devopslearning.repositories.ParcourRepository;
import org.example.devopslearning.services.ClasseStudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/structure")
public class AdminStructureController {

    private final FiliereRepository filiereRepository;
    private final NiveauxEtudeRepository niveauxEtudeRepository;
    private final ParcourRepository parcourRepository;
    private final AcademicClassRepository classRepository;
    private final ClasseStudentService classeStudentService;

    @GetMapping
    public String page(Model model) {
        // Charger toutes les listes
        List<Filiere> filieres = filiereRepository.findAll();
        List<NiveauxEtude> niveaux = niveauxEtudeRepository.findAll();
        List<Parcour> parcours = parcourRepository.findAll();
        List<AcademicClass> classes = classRepository.findAll();

        // ✅ Créer une map avec le comptage des parcours par filière
        java.util.Map<Long, Long> parcoursCounts = new java.util.HashMap<>();
        for (Filiere filiere : filieres) {
            long count = parcourRepository.countByFiliereId(filiere.getId());
            parcoursCounts.put(filiere.getId(), count);
        }

        // ✅ Créer une map avec le comptage des classes par parcours
        java.util.Map<Long, Long> classesCounts = new java.util.HashMap<>();
        for (Parcour parcour : parcours) {
            long count = classRepository.countByParcoursId(parcour.getId());
            classesCounts.put(parcour.getId(), count);
        }

        // ✅ Créer une map avec le comptage des étudiants par classe
        java.util.Map<Long, Long> studentCounts = new java.util.HashMap<>();
        for (AcademicClass classe : classes) {
            long count = classeStudentService.countActiveStudentsInClass(classe.getId());
            studentCounts.put(classe.getId(), count);
        }

        model.addAttribute("filieres", filieres);
        model.addAttribute("niveaux", niveaux);
        model.addAttribute("parcoursList", parcours);
        model.addAttribute("classes", classes);
        model.addAttribute("parcoursCounts", parcoursCounts);    // ✅ NOUVEAU
        model.addAttribute("classesCounts", classesCounts);      // ✅ NOUVEAU
        model.addAttribute("studentCounts", studentCounts);

        // Form objects vides
        model.addAttribute("filiere", new Filiere());
        model.addAttribute("parcour", new Parcour());
        model.addAttribute("classe", new AcademicClass());

        return "admin/structure";
    }

    // ========== FILIÈRES ==========

    @PostMapping("/filieres")
    public String createFiliere(@ModelAttribute Filiere f, RedirectAttributes ra) {
        try {
            filiereRepository.save(f);
            ra.addFlashAttribute("success", "Filière créée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    @GetMapping("/filieres/{id}")
    public String viewFiliere(@PathVariable Long id, Model model) {
        Filiere filiere = filiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));

        // Compter les parcours associés
        long nbParcours = parcourRepository.countByFiliereId(id);

        model.addAttribute("filiere", filiere);
        model.addAttribute("nbParcours", nbParcours);

        return "admin/filiere-detail";
    }

    @GetMapping("/filieres/{id}/edit")
    public String editFiliereForm(@PathVariable Long id, Model model) {
        Filiere filiere = filiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
        model.addAttribute("filiere", filiere);
        return "admin/filiere-edit";
    }

    @PostMapping("/filieres/{id}/edit")
    public String updateFiliere(@PathVariable Long id,
                                @ModelAttribute Filiere f,
                                RedirectAttributes ra) {
        try {
            Filiere existing = filiereRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée"));

            existing.setNom(f.getNom());
            existing.setDescription(f.getDescription());
            filiereRepository.save(existing);

            ra.addFlashAttribute("success", "Filière modifiée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    @PostMapping("/filieres/{id}/delete")
    public String deleteFiliere(@PathVariable Long id, RedirectAttributes ra) {
        try {
            // Vérifier s'il y a des parcours associés
            long nbParcours = parcourRepository.countByFiliereId(id);
            if (nbParcours > 0) {
                ra.addFlashAttribute("error", "Impossible de supprimer : " + nbParcours + " parcours associé(s)");
                return "redirect:/admin/structure";
            }

            filiereRepository.deleteById(id);
            ra.addFlashAttribute("success", "Filière supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    // ========== PARCOURS ==========

    @PostMapping("/parcours")
    public String createParcours(@ModelAttribute Parcour p, RedirectAttributes ra) {
        try {
            parcourRepository.save(p);
            ra.addFlashAttribute("success", "Parcours créé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    @GetMapping("/parcours/{id}")
    public String viewParcours(@PathVariable Long id, Model model) {
        Parcour parcours = parcourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        // Compter les classes associées
        long nbClasses = classRepository.countByParcoursId(id);

        model.addAttribute("parcours", parcours);
        model.addAttribute("nbClasses", nbClasses);

        return "admin/parcours-detail";
    }

    @GetMapping("/parcours/{id}/edit")
    public String editParcoursForm(@PathVariable Long id, Model model) {
        Parcour parcours = parcourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        List<Filiere> filieres = filiereRepository.findAll();
        List<NiveauxEtude> niveaux = niveauxEtudeRepository.findAll();

        model.addAttribute("parcours", parcours);
        model.addAttribute("filieres", filieres);
        model.addAttribute("niveaux", niveaux);

        return "admin/parcours-edit";
    }

    @PostMapping("/parcours/{id}/edit")
    public String updateParcours(@PathVariable Long id,
                                 @ModelAttribute Parcour p,
                                 RedirectAttributes ra) {
        try {
            Parcour existing = parcourRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            existing.setNom(p.getNom());
            existing.setDescription(p.getDescription());
            existing.setFiliere(p.getFiliere());
            existing.setNiveau(p.getNiveau());
            parcourRepository.save(existing);

            ra.addFlashAttribute("success", "Parcours modifié avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    @PostMapping("/parcours/{id}/delete")
    public String deleteParcours(@PathVariable Long id, RedirectAttributes ra) {
        try {
            // Vérifier s'il y a des classes associées
            long nbClasses = classRepository.countByParcoursId(id);
            if (nbClasses > 0) {
                ra.addFlashAttribute("error", "Impossible de supprimer : " + nbClasses + " classe(s) associée(s)");
                return "redirect:/admin/structure";
            }

            parcourRepository.deleteById(id);
            ra.addFlashAttribute("success", "Parcours supprimé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    // ========== CLASSES ==========

    @PostMapping("/classes")
    public String createClasse(@ModelAttribute AcademicClass c, RedirectAttributes ra) {
        try {
            classRepository.save(c);
            ra.addFlashAttribute("success", "Classe créée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    // ✅ MÉTHODE UNIQUE viewClasse AVEC GESTION DES ÉTUDIANTS
    @GetMapping("/classes/{id}")
    public String viewClasse(@PathVariable Long id, Model model) {
        AcademicClass classe = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        // ✅ Récupérer les inscriptions avec détails
        List<InscriptionsClass> inscriptions = classeStudentService.getInscriptionsForClass(id);
        long nbEtudiants = classeStudentService.countActiveStudentsInClass(id);

        // ✅ Récupérer les étudiants disponibles (non inscrits)
        List<User> availableStudents = classeStudentService.getAvailableStudents(id);

        model.addAttribute("classe", classe);
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("nbEtudiants", nbEtudiants);
        model.addAttribute("availableStudents", availableStudents);

        return "admin/classe-detail";
    }

    @GetMapping("/classes/{id}/edit")
    public String editClasseForm(@PathVariable Long id, Model model) {
        AcademicClass classe = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        List<Parcour> parcours = parcourRepository.findAll();

        model.addAttribute("classe", classe);
        model.addAttribute("parcoursList", parcours);

        return "admin/classe-edit";
    }

    @PostMapping("/classes/{id}/edit")
    public String updateClasse(@PathVariable Long id,
                               @ModelAttribute AcademicClass c,
                               RedirectAttributes ra) {
        try {
            AcademicClass existing = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

            existing.setNom(c.getNom());
            existing.setCode(c.getCode());
            existing.setAnneeUniversitaire(c.getAnneeUniversitaire());
            existing.setParcours(c.getParcours());
            classRepository.save(existing);

            ra.addFlashAttribute("success", "Classe modifiée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClasse(@PathVariable Long id, RedirectAttributes ra) {
        try {
            classRepository.deleteById(id);
            ra.addFlashAttribute("success", "Classe supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure";
    }

    // ========== GESTION DES ÉTUDIANTS ==========

    @PostMapping("/classes/{classeId}/students/enroll")
    public String enrollStudent(@PathVariable Long classeId,
                                @RequestParam Long etudiantId,
                                RedirectAttributes ra) {
        try {
            classeStudentService.enrollStudent(classeId, etudiantId);
            ra.addFlashAttribute("success", "Étudiant inscrit avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/unenroll")
    public String unenrollStudent(@PathVariable Long classeId,
                                  @PathVariable Long etudiantId,
                                  RedirectAttributes ra) {
        try {
            classeStudentService.unenrollStudent(classeId, etudiantId);
            ra.addFlashAttribute("success", "Étudiant retiré de la classe");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/reactivate")
    public String reactivateStudent(@PathVariable Long classeId,
                                    @PathVariable Long etudiantId,
                                    RedirectAttributes ra) {
        try {
            classeStudentService.reactivateStudent(classeId, etudiantId);
            ra.addFlashAttribute("success", "Étudiant réactivé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/{etudiantId}/delete")
    public String deleteEnrollment(@PathVariable Long classeId,
                                   @PathVariable Long etudiantId,
                                   RedirectAttributes ra) {
        try {
            classeStudentService.deleteEnrollment(classeId, etudiantId);
            ra.addFlashAttribute("success", "Inscription supprimée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }

    @PostMapping("/classes/{classeId}/students/enroll-multiple")
    public String enrollMultipleStudents(@PathVariable Long classeId,
                                         @RequestParam("etudiantIds") List<Long> etudiantIds,
                                         RedirectAttributes ra) {
        try {
            classeStudentService.enrollMultipleStudents(classeId, etudiantIds);
            ra.addFlashAttribute("success", etudiantIds.size() + " étudiant(s) inscrit(s)");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/structure/classes/" + classeId;
    }
}