package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherProgressionController {

    @GetMapping("/progression")
    public String progression(Model model, Authentication authentication) {
        model.addAttribute("currentPath", "/teacher/progression");
        // Ajoute ici tes données (liste d'étudiants, stats, etc.)
        return "teacher/progression"; // → resources/templates/teacher/progression.html
    }
}