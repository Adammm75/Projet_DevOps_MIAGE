package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.MessageCreateRequest;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.MessagingService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;
    private final UserService userService;

    @GetMapping
    public String inbox(Authentication auth, Model model) {
        User current = userService.findByEmail(auth.getName());

        model.addAttribute("inbox", messagingService.inbox(current.getId()));
        model.addAttribute("sent", messagingService.sent(current.getId()));
        model.addAttribute("messageForm", new MessageCreateRequest());

        // ✅ AJOUTÉ : Liste des enseignants et étudiants
        List<User> allUsers = userService.getAllUsers();

        // Séparer enseignants et étudiants
        List<User> teachers = allUsers.stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equals("ROLE_TEACHER")))
                .collect(Collectors.toList());

        List<User> students = allUsers.stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equals("ROLE_STUDENT")))
                .collect(Collectors.toList());

        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);

        return "messages/inbox";
    }

    @PostMapping
    public String send(@ModelAttribute("messageForm") @Valid MessageCreateRequest form,
                       BindingResult bindingResult,
                       Authentication auth,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return inbox(auth, model);
        }
        User sender = userService.findByEmail(auth.getName());
        messagingService.sendMessage(
                sender,
                form.getRecipientEmail(),
                form.getCourseId(),
                form.getSubject(),
                form.getContent()
        );
        return "redirect:/messages";
    }

    @PostMapping("/{messageId}/read")
    public String markRead(@PathVariable Long messageId) {
        messagingService.markAsRead(messageId);
        return "redirect:/messages";
    }
}