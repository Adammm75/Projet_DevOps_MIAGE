package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.QuizService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/qcm")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    @GetMapping("/course/{courseId}")
    public String list(@PathVariable Long courseId, Model model) {
        model.addAttribute("qcms", quizService.listPublishedByCourse(courseId));
        return "qcm/list";
    }

    @GetMapping("/{qcmId}/start")
    public String start(@PathVariable Long qcmId, Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());
        model.addAttribute("tentative", quizService.startQuiz(qcmId, student.getId()));
        model.addAttribute("questions", quizService.getQuestions(qcmId));
        return "qcm/take";
    }

    @PostMapping("/{qcmId}/finish")
    public String finish(@PathVariable Long qcmId,
                         @RequestParam Long tentativeId) {
        quizService.finishQuiz(tentativeId);
        return "redirect:/dashboard";
    }
}
