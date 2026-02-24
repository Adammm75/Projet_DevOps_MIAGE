package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.MessagingService;
import org.example.devopslearning.services.StudentDashboardService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final CoursService coursService;
    private final StudentDashboardService studentDashboardService;
    private final MessagingService messagingService;  // ✅ AJOUTÉ

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User current = userService.findByEmail(auth.getName());
        model.addAttribute("currentUser", current);

        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
        boolean isTeacher = hasRole(auth, "ROLE_TEACHER");
        boolean isStudent = hasRole(auth, "ROLE_STUDENT");

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("isStudent", isStudent);

        if (isTeacher) {
            model.addAttribute("courses", coursService.getCoursesByTeacher(current));
            // ✅ AJOUTÉ : Messages non lus
            addUnreadMessagesToModel(model, auth.getName());
            return "dashboard/teacher-dashboard";
        }

        if (isStudent) {
            Map<String, Object> data = studentDashboardService.buildDashboard(current.getId());
            model.addAllAttributes(data);
            // ✅ AJOUTÉ : Messages non lus
            addUnreadMessagesToModel(model, auth.getName());
            return "dashboard/student-dashboard";
        }

        model.addAttribute("courses", coursService.listAll());
        // ✅ AJOUTÉ : Messages non lus
        addUnreadMessagesToModel(model, auth.getName());
        return "dashboard/admin-dashboard";
    }

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals(role)) return true;
        }
        return false;
    }

    /**
     * ✅ MÉTHODE AJOUTÉE : Ajoute le nombre de messages non lus au model
     */
    private void addUnreadMessagesToModel(Model model, String userEmail) {
        User currentUser = userService.findByEmail(userEmail);
        long unreadCount = messagingService.countUnreadMessages(currentUser.getId());
        model.addAttribute("unreadMessages", unreadCount);
    }
}