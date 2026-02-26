package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService              userService;
    private final CoursService             coursService;
    private final StudentDashboardService  studentDashboardService;
    private final MessagingService         messagingService;
    private final TeacherReminderService   reminderService; // ✅ AJOUTÉ

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User current = userService.findByEmail(auth.getName());
        model.addAttribute("currentUser", current);

        boolean isAdmin   = hasRole(auth, "ROLE_ADMIN");
        boolean isTeacher = hasRole(auth, "ROLE_TEACHER");
        boolean isStudent = hasRole(auth, "ROLE_STUDENT");

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("isStudent", isStudent);

        if (isTeacher) {
            model.addAttribute("courses", coursService.getCoursesByTeacher(current));
            addUnreadMessagesToModel(model, current);

            // ✅ Rappels automatiques
            List<TeacherReminderService.ReminderDTO> reminders =
                    reminderService.getRemindersForTeacher(current);
            long criticalCount = reminders.stream()
                    .filter(r -> r.getLevel() == TeacherReminderService.ReminderLevel.CRITIQUE)
                    .count();

            model.addAttribute("reminders", reminders);
            model.addAttribute("reminderCount", reminders.size());
            model.addAttribute("criticalCount", criticalCount);

            return "dashboard/teacher-dashboard";
        }

        if (isStudent) {
            Map<String, Object> data = studentDashboardService.buildDashboard(current.getId());
            model.addAllAttributes(data);
            addUnreadMessagesToModel(model, current);
            return "dashboard/student-dashboard";
        }

        model.addAttribute("courses", coursService.listAll());
        addUnreadMessagesToModel(model, current);
        return "dashboard/admin-dashboard";
    }

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals(role)) return true;
        }
        return false;
    }

    private void addUnreadMessagesToModel(Model model, User user) {
        long unreadCount = messagingService.countUnreadMessages(user.getId());
        model.addAttribute("unreadMessages", unreadCount);
    }
}