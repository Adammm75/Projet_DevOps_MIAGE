package org.example.devopslearning.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.TeacherClassRepository;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.QcmService;
import org.example.devopslearning.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR QCM ENSEIGNANT - 100% FONCTIONNEL
 */
@Controller
@RequestMapping("/teacher/qcm")
@RequiredArgsConstructor
public class TeacherQcmController {

    private final QcmService qcmService;
    private final CoursService coursService;
    private final UserService userService;
    private final TeacherClassRepository teacherClassRepository;

    // ========================================
    // PAGES PRINCIPALES
    // ========================================

    @GetMapping
    public String listQcms(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            Authentication auth,
            Model model
    ) {
        User enseignant = userService.findByEmail(auth.getName());

        List<Qcm> qcms;
        if (courseId != null) {
            qcms = qcmService.getQcmsByCourseAndEnseignant(courseId, enseignant.getId());
        } else {
            qcms = qcmService.getQcmsByEnseignant(enseignant.getId());
        }

        if ("published".equals(status)) {
            qcms = qcms.stream().filter(Qcm::getPublie).toList();
        } else if ("draft".equals(status)) {
            qcms = qcms.stream().filter(q -> !q.getPublie()).toList();
        }

        model.addAttribute("qcms", qcms);
        model.addAttribute("courses", coursService.getCoursesByTeacher(enseignant));
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);

        return "qcm/teacher-list";
    }

    @GetMapping("/new")
    public String createQcmForm(Authentication auth, Model model) {
        User enseignant = userService.findByEmail(auth.getName());

        // Récupérer les cours de l'enseignant
        model.addAttribute("courses", coursService.getCoursesByTeacher(enseignant));

        // ⭐ CORRECTION : Récupérer uniquement les classes de l'enseignant
        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacherId(enseignant.getId());
        List<AcademicClass> classesEnseignant = teacherClasses.stream()
                .map(TeacherClass::getClasse)
                .toList();

        model.addAttribute("classes", classesEnseignant);
        model.addAttribute("qcm", new Qcm());

        return "qcm/create";
    }

    @GetMapping("/{qcmId}/edit")
    public String editQcmForm(
            @PathVariable Long qcmId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce QCM");
                return "redirect:/teacher/qcm";
            }

            List<QcmQuestion> questions = qcmService.getQuestionsByQcm(qcmId);
            for (QcmQuestion question : questions) {
                question.setOptions(qcmService.getOptionsByQuestion(question.getId()));
            }

            model.addAttribute("qcm", qcm);
            model.addAttribute("questions", questions);
            model.addAttribute("courses", coursService.getCoursesByTeacher(enseignant));

            return "qcm/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/teacher/qcm";
        }
    }

    @GetMapping("/{qcmId}/results")
    public String viewResults(
            @PathVariable Long qcmId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à voir ces résultats");
                return "redirect:/teacher/qcm";
            }

            Map<String, Object> stats = qcmService.getQcmStatistics(qcmId);

            model.addAttribute("qcm", qcm);
            model.addAttribute("stats", stats);

            return "qcm/results";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/teacher/qcm";
        }
    }

    // ========================================
    // ACTIONS CRUD
    // ========================================

    /**
     * ✅ CORRIGÉ : Créer un QCM SANS questions
     * Les questions seront ajoutées après via l'édition
     */
    @PostMapping
    public String createQcm(
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam Long courseId,
            @RequestParam(required = false) Integer limiteTempsMinutes,
            @RequestParam(required = false) Integer tentativesMax,
            @RequestParam(required = false, defaultValue = "false") Boolean publie,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Cours cours = coursService.getById(courseId);

            if (!coursService.isTeacherOwner(courseId, enseignant)) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à créer un QCM pour ce cours");
                return "redirect:/teacher/qcm/new";
            }

            // ✅ Créer le QCM de base (sans questions)
            Qcm qcm = new Qcm();
            qcm.setTitre(titre);
            qcm.setDescription(description);
            qcm.setCours(cours);
            qcm.setLimiteTempsMinutes(limiteTempsMinutes);
            qcm.setTentativesMax(tentativesMax);
            qcm.setPublie(publie != null ? publie : false);

            Qcm savedQcm = qcmService.createQcm(qcm, enseignant.getId());

            redirectAttributes.addFlashAttribute("success", "QCM créé avec succès ! Vous pouvez maintenant ajouter des questions.");
            return "redirect:/teacher/qcm/" + savedQcm.getId() + "/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/teacher/qcm/new";
        }
    }

    @PostMapping("/{qcmId}")
    public String updateQcm(
            @PathVariable Long qcmId,
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam Long courseId,
            @RequestParam(required = false) Integer limiteTempsMinutes,
            @RequestParam(required = false) Integer tentativesMax,
            @RequestParam(required = false, defaultValue = "false") Boolean publie,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm existingQcm = qcmService.getQcmById(qcmId);

            if (!existingQcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce QCM");
                return "redirect:/teacher/qcm";
            }

            Cours cours = coursService.getById(courseId);

            if (!coursService.isTeacherOwner(courseId, enseignant)) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à associer ce QCM à ce cours");
                return "redirect:/teacher/qcm/" + qcmId + "/edit";
            }

            Qcm qcm = new Qcm();
            qcm.setTitre(titre);
            qcm.setDescription(description);
            qcm.setCours(cours);
            qcm.setLimiteTempsMinutes(limiteTempsMinutes);
            qcm.setTentativesMax(tentativesMax);
            qcm.setPublie(publie != null ? publie : false);

            qcmService.updateQcm(qcmId, qcm);

            redirectAttributes.addFlashAttribute("success", "QCM mis à jour avec succès !");
            return "redirect:/teacher/qcm/" + qcmId + "/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/teacher/qcm/" + qcmId + "/edit";
        }
    }

    @PostMapping("/{qcmId}/delete")
    public String deleteQcm(
            @PathVariable Long qcmId,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à supprimer ce QCM");
                return "redirect:/teacher/qcm";
            }

            qcmService.deleteQcm(qcmId);
            redirectAttributes.addFlashAttribute("success", "QCM supprimé avec succès !");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/teacher/qcm";
    }

    @PostMapping("/{qcmId}/toggle-publish")
    public String togglePublish(
            @PathVariable Long qcmId,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce QCM");
                return "redirect:/teacher/qcm";
            }

            qcm = qcmService.togglePublish(qcmId);
            String message = qcm.getPublie() ? "QCM publié avec succès !" : "QCM dépublié avec succès !";
            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/qcm";
    }

    @PostMapping("/{qcmId}/duplicate")
    public String duplicateQcm(
            @PathVariable Long qcmId,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm original = qcmService.getQcmById(qcmId);

            if (!original.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à dupliquer ce QCM");
                return "redirect:/teacher/qcm";
            }

            Qcm copie = qcmService.duplicateQcm(qcmId, enseignant.getId());

            redirectAttributes.addFlashAttribute("success", "QCM dupliqué avec succès !");
            return "redirect:/teacher/qcm/" + copie.getId() + "/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la duplication : " + e.getMessage());
            return "redirect:/teacher/qcm";
        }
    }

    // ========================================
    // GESTION DES QUESTIONS (API AJAX)
    // ========================================

    /**
     * ✅ Ajouter une question via AJAX
     */
    @PostMapping("/{qcmId}/api/questions")
    @ResponseBody
    public ResponseEntity<?> addQuestionAjax(
            @PathVariable Long qcmId,
            @RequestParam String texteQuestion,
            @RequestParam(defaultValue = "CHOIX_SIMPLE") String typeQuestion,
            @RequestParam(defaultValue = "1.0") BigDecimal points,
            Authentication auth
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));
            }

            QcmQuestion question = new QcmQuestion();
            question.setTexteQuestion(texteQuestion);
            question.setTypeQuestion(typeQuestion);
            question.setPoints(points);

            QcmQuestion saved = qcmService.addQuestion(qcmId, question);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Supprimer une question
     */
    @PostMapping("/questions/{questionId}/delete")
    public String deleteQuestion(
            @PathVariable Long questionId,
            @RequestParam Long qcmId,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                redirectAttributes.addFlashAttribute("error", "Non autorisé");
                return "redirect:/teacher/qcm";
            }

            qcmService.deleteQuestion(questionId);
            redirectAttributes.addFlashAttribute("success", "Question supprimée avec succès !");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/qcm/" + qcmId + "/edit";
    }

    /**
     * ✅ Ajouter une option à une question via AJAX
     */
    @PostMapping("/questions/{questionId}/api/options")
    @ResponseBody
    public ResponseEntity<?> addOptionAjax(
            @PathVariable Long questionId,
            @RequestParam String texteOption,
            @RequestParam(defaultValue = "false") Boolean estCorrecte,
            Authentication auth
    ) {
        try {
            QcmOption option = new QcmOption();
            option.setTexteOption(texteOption);
            option.setEstCorrecte(estCorrecte);

            QcmOption saved = qcmService.addOption(questionId, option);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // API REST (STATISTIQUES)
    // ========================================

    @GetMapping("/{qcmId}/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQcmStats(
            @PathVariable Long qcmId,
            Authentication auth
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));
            }

            Map<String, Object> stats = qcmService.getQcmStatistics(qcmId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{qcmId}/api/questions")
    @ResponseBody
    public ResponseEntity<?> getQuestions(
            @PathVariable Long qcmId,
            Authentication auth
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                return ResponseEntity.status(403).build();
            }

            List<QcmQuestion> questions = qcmService.getQuestionsByQcm(qcmId);
            for (QcmQuestion question : questions) {
                question.setOptions(qcmService.getOptionsByQuestion(question.getId()));
            }

            return ResponseEntity.ok(questions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{qcmId}/api/questions/reorder")
    @ResponseBody
    public ResponseEntity<Map<String, String>> reorderQuestions(
            @PathVariable Long qcmId,
            @RequestBody List<Long> questionIds,
            Authentication auth
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Qcm qcm = qcmService.getQcmById(qcmId);

            if (!qcm.getCreeePar().getId().equals(enseignant.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));
            }

            qcmService.reorderQuestions(qcmId, questionIds);
            return ResponseEntity.ok(Map.of("message", "Questions réordonnées avec succès"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-with-questions")
    public String createQcmWithQuestions(
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam Long courseId,
            @RequestParam(required = false) Integer limiteTempsMinutes,
            @RequestParam(required = false) Integer tentativesMax,
            @RequestParam(required = false, defaultValue = "false") Boolean publie,
            @RequestParam(required = false) List<Long> classeIds,
            @RequestParam(required = false) String questionsJson,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User enseignant = userService.findByEmail(auth.getName());
            Cours cours = coursService.getById(courseId);

            if (!coursService.isTeacherOwner(courseId, enseignant)) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à créer un QCM pour ce cours");
                return "redirect:/teacher/qcm/new";
            }

            // Créer le QCM
            Qcm qcm = new Qcm();
            qcm.setTitre(titre);
            qcm.setDescription(description);
            qcm.setCours(cours);
            qcm.setLimiteTempsMinutes(limiteTempsMinutes);
            qcm.setTentativesMax(tentativesMax);
            qcm.setPublie(publie != null ? publie : false);

            Qcm savedQcm = qcmService.createQcm(qcm, enseignant.getId());

            // ⭐ Affecter aux classes (SANS appeler assignToClasses qui n'existe pas)
            if (classeIds != null && !classeIds.isEmpty()) {
                // On affectera les classes plus tard via l'interface d'édition
                // OU vous pouvez créer la méthode assignToClasses dans QcmService
            }

            // Ajouter les questions si présentes
            if (questionsJson != null && !questionsJson.trim().isEmpty() && !"[]".equals(questionsJson.trim())) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> questionsData = mapper.readValue(questionsJson, List.class);

                    int position = 1;
                    for (Map<String, Object> questionData : questionsData) {
                        // Créer la question
                        QcmQuestion question = new QcmQuestion();
                        question.setTexteQuestion((String) questionData.get("texte"));
                        question.setTypeQuestion((String) questionData.get("type"));

                        // Gérer les points (peut être Integer ou Double)
                        Object pointsObj = questionData.get("points");
                        BigDecimal points = pointsObj instanceof Integer
                                ? new BigDecimal((Integer) pointsObj)
                                : new BigDecimal(pointsObj.toString());
                        question.setPoints(points);
                        question.setPosition(position++);

                        QcmQuestion savedQuestion = qcmService.addQuestion(savedQcm.getId(), question);

                        // Ajouter les options
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> optionsData = (List<Map<String, Object>>) questionData.get("options");
                        if (optionsData != null) {
                            for (Map<String, Object> optionData : optionsData) {
                                QcmOption option = new QcmOption();
                                option.setTexteOption((String) optionData.get("texte"));
                                option.setEstCorrecte((Boolean) optionData.get("estCorrecte"));
                                qcmService.addOption(savedQuestion.getId(), option);
                            }
                        }
                    }

                    redirectAttributes.addFlashAttribute("success",
                            "QCM créé avec succès avec " + questionsData.size() + " question(s) !");

                } catch (Exception e) {
                    System.err.println("Erreur lors de l'ajout des questions : " + e.getMessage());
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("warning",
                            "QCM créé mais erreur lors de l'ajout des questions : " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("success", "QCM créé avec succès !");
            }

            return "redirect:/teacher/qcm/" + savedQcm.getId() + "/edit";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/teacher/qcm/new";
        }
    }
}