package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.MessageCreateRequest;
import org.example.devopslearning.entities.Cours;
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

        List<User> allUsers = userService.getAllUsers();

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

    /**
     * ✅ Page de composition avec pré-remplissage du destinataire
     * Appelée depuis le bouton "Contacter l'enseignant" : /messages/compose?recipientId=X
     */
    @GetMapping("/compose")
    public String compose(@RequestParam(required = false) Long recipientId,
                          @RequestParam(required = false) Long courseId,
                          Authentication auth,
                          Model model) {
        User current = userService.findByEmail(auth.getName());

        String recipientEmail = "";
        String recipientName = "";
        if (recipientId != null) {
            try {
                User recipient = userService.findById(recipientId);
                recipientEmail = recipient.getEmail();
                recipientName = recipient.getFirstName() + " " + recipient.getLastName();
            } catch (Exception ignored) {}
        }

        List<Cours> courses = userService.getCoursesForUser(current);

        model.addAttribute("recipientEmail", recipientEmail);
        model.addAttribute("recipientName", recipientName);
        model.addAttribute("preselectedCourseId", courseId);
        model.addAttribute("courses", courses);

        return "messages/compose";
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

    /**
     * ✅ Traitement du formulaire compose
     */
    @PostMapping("/compose")
    public String sendFromCompose(@RequestParam String recipientEmail,
                                  @RequestParam(required = false) Long courseId,
                                  @RequestParam String subject,
                                  @RequestParam String content,
                                  Authentication auth) {
        User sender = userService.findByEmail(auth.getName());
        messagingService.sendMessage(sender, recipientEmail, courseId, subject, content);
        return "redirect:/messages?sent=true";
    }

    @PostMapping("/{messageId}/read")
    public String markRead(@PathVariable Long messageId) {
        messagingService.markAsRead(messageId);
        return "redirect:/messages";
    }
}