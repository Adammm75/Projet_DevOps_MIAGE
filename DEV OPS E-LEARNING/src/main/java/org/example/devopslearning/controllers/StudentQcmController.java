package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.services.QcmService;
import org.example.devopslearning.services.StudentQcmService;
import org.example.devopslearning.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * ❓ CONTRÔLEUR QCM ÉTUDIANT - COMPLET
 */
@Controller
@RequestMapping("/student/qcm")
@RequiredArgsConstructor
public class StudentQcmController {

    private final StudentQcmService studentQcmService;
    private final QcmService qcmService;
    private final UserService userService;

    // ========================================
    // LISTE DES QCM
    // ========================================

    /**
     * Liste tous les QCM disponibles pour l'étudiant
     */
    @GetMapping
    public String list(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Qcm> availableQcms = studentQcmService.getAvailableQcms(student.getId());

        // ✅ AJOUTEZ CETTE LIGNE DE DEBUG
        System.out.println("🔍 DEBUG - Nombre de QCM trouvés : " + availableQcms.size());
        System.out.println("🔍 DEBUG - Student ID : " + student.getId());

        List<QcmTentative> attempts = studentQcmService.getAllAttempts(student.getId());

        model.addAttribute("qcms", availableQcms);
        model.addAttribute("attempts", attempts);
        model.addAttribute("totalQcms", availableQcms.size());

        return "qcm/student-qcm-list";
    }

    /**
     * Liste des QCM d'un cours spécifique
     */
    @GetMapping("/course/{courseId}")
    public String listByCourse(@PathVariable Long courseId,
                               Authentication auth,
                               Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Qcm> qcms = studentQcmService.getQcmsByCourse(student.getId(), courseId);

        model.addAttribute("qcms", qcms);
        model.addAttribute("courseId", courseId);

        return "qcm/student-qcm-list";
    }

    // ========================================
    // PASSER UN QCM
    // ========================================

    /**
     * Démarrer un QCM
     */
    @GetMapping("/{qcmId}/start")
    public String startQcm(@PathVariable Long qcmId,
                           Authentication auth,
                           Model model,
                           RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // Vérifier l'accès
            if (!studentQcmService.canAccessQcm(student.getId(), qcmId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce QCM");
                return "redirect:/student/qcm";
            }

            // Vérifier le nombre de tentatives
            if (!studentQcmService.canStartNewAttempt(student.getId(), qcmId)) {
                ra.addFlashAttribute("error", "Nombre maximum de tentatives atteint");
                return "redirect:/student/qcm";
            }

            // Créer une nouvelle tentative
            QcmTentative tentative = studentQcmService.startAttempt(qcmId, student.getId());

            return "redirect:/student/qcm/attempt/" + tentative.getId();

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/qcm";
        }
    }

    /**
     * Afficher le QCM (tentative en cours)
     */
    @GetMapping("/attempt/{attemptId}")
    public String takeQcm(@PathVariable Long attemptId,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            QcmTentative tentative = studentQcmService.getAttempt(attemptId);

            // Vérifier que c'est la tentative de l'étudiant
            if (!tentative.getEtudiant().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/qcm";
            }

            // Vérifier que la tentative n'est pas terminée
            if ("TERMINE".equals(tentative.getStatut())) {
                return "redirect:/student/qcm/result/" + attemptId;
            }

            Qcm qcm = tentative.getQcm();
            List<QcmQuestion> questions = qcmService.getQuestionsByQcm(qcm.getId());

            // Charger les options pour chaque question
            for (QcmQuestion question : questions) {
                question.setOptions(qcmService.getOptionsByQuestion(question.getId()));
            }

            model.addAttribute("tentative", tentative);
            model.addAttribute("qcm", qcm);
            model.addAttribute("questions", questions);

            return "qcm/student-qcm-take";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/qcm";
        }
    }

    // ========================================
    // SOUMETTRE LES RÉPONSES
    // ========================================

    /**
     * Enregistrer une réponse (AJAX)
     */
    @PostMapping("/attempt/{attemptId}/answer")
    @ResponseBody
    public ResponseEntity<?> saveAnswer(@PathVariable Long attemptId,
                                        @RequestParam Long questionId,
                                        @RequestParam Long optionId,
                                        Authentication auth) {
        try {
            User student = userService.findByEmail(auth.getName());
            QcmTentative tentative = studentQcmService.getAttempt(attemptId);

            if (!tentative.getEtudiant().getId().equals(student.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));
            }

            studentQcmService.saveAnswer(attemptId, questionId, optionId);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Terminer le QCM et calculer le score
     */
    @PostMapping("/attempt/{attemptId}/finish")
    public String finishQcm(@PathVariable Long attemptId,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            QcmTentative tentative = studentQcmService.getAttempt(attemptId);

            if (!tentative.getEtudiant().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Non autorisé");
                return "redirect:/student/qcm";
            }

            studentQcmService.finishAttempt(attemptId);

            ra.addFlashAttribute("success", "QCM terminé avec succès !");
            return "redirect:/student/qcm/result/" + attemptId;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/qcm/attempt/" + attemptId;
        }
    }

    // ========================================
    // RÉSULTATS
    // ========================================

    /**
     * Voir les résultats d'une tentative
     */
    @GetMapping("/result/{attemptId}")
    public String viewResult(@PathVariable Long attemptId,
                             Authentication auth,
                             Model model,
                             RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            QcmTentative tentative = studentQcmService.getAttempt(attemptId);

            if (!tentative.getEtudiant().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Non autorisé");
                return "redirect:/student/qcm";
            }

            Qcm qcm = tentative.getQcm();
            List<QcmRepons> reponses = studentQcmService.getResponses(attemptId);

            model.addAttribute("tentative", tentative);
            model.addAttribute("qcm", qcm);
            model.addAttribute("reponses", reponses);

            return "qcm/student-qcm-result";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/qcm";
        }
    }

    /**
     * Historique des tentatives pour un QCM
     */
    @GetMapping("/{qcmId}/attempts")
    public String viewAttempts(@PathVariable Long qcmId,
                               Authentication auth,
                               Model model,
                               RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            if (!studentQcmService.canAccessQcm(student.getId(), qcmId)) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/qcm";
            }

            Qcm qcm = qcmService.getQcmById(qcmId);
            List<QcmTentative> attempts = studentQcmService.getAttemptsByQcm(student.getId(), qcmId);

            model.addAttribute("qcm", qcm);
            model.addAttribute("attempts", attempts);

            return "qcm/student-qcm-attempts";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/qcm";
        }
    }

}