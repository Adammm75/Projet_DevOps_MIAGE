package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.LoginRequest;
import org.example.devopslearning.dto.RegisterRequest;
import org.example.devopslearning.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // PAGE LOGIN
    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginRequest());
        }
        return "auth/login";
    }

    // PAGE REGISTER
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterRequest());
        }
        return "auth/register";
    }

    // TRAITEMENT REGISTER
    @PostMapping("/register")
    public String doRegister(@ModelAttribute("registerForm") @Valid RegisterRequest form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez remplir tous les champs correctement");
            redirectAttributes.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }

        try {
            // Inscription avec rôle STUDENT par défaut
            userService.registerUser(form, "ROLE_STUDENT");
            redirectAttributes.addFlashAttribute("success", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
    }
}