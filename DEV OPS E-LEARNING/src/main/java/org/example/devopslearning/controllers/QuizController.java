package org.example.devopslearning.controllers;

/**
 * ❌ ANCIEN CONTRÔLEUR - OBSOLÈTE
 *
 * Ce contrôleur a été remplacé par StudentQcmController.java
 * Il est commenté pour éviter les conflits de routes.
 *
 * Vous pouvez SUPPRIMER ce fichier en toute sécurité.
 */

/*
 * @Controller
 * 
 * @RequiredArgsConstructor
 * 
 * @RequestMapping("/student/qcm")
 * public class QuizController {
 * 
 * private final QcmService qcmService;
 * private final UserService userService;
 * 
 * @GetMapping("/course/{courseId}")
 * public String list(@PathVariable Long courseId, Model model) {
 * model.addAttribute("qcms", qcmService.getPublishedQcmsByCourse(courseId));
 * return "qcm/list";
 * }
 * 
 * @GetMapping("/{qcmId}/start")
 * public String start(@PathVariable Long qcmId, Authentication auth, Model
 * model) {
 * User student = userService.findByEmail(auth.getName());
 * model.addAttribute("tentative", qcmService.startTentative(qcmId,
 * student.getId()));
 * model.addAttribute("questions", qcmService.getQuestionsByQcm(qcmId));
 * return "qcm/take";
 * }
 * 
 * @PostMapping("/{qcmId}/finish")
 * public String finish(@PathVariable Long qcmId, @RequestParam Long
 * tentativeId) {
 * qcmService.finishTentative(tentativeId);
 * return "redirect:/dashboard";
 * }
 * }
 */