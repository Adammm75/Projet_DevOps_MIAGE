package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TeacherCommentController {

    private final UserService            userService;
    private final TeacherCommentService  commentService;
    private final CoursService           coursService;

    // ========================================
    // TEACHER : Fiche étudiant avec commentaires
    // GET /teacher/students/{studentId}/comments
    // ========================================

    @GetMapping("/teacher/students/{studentId}/comments")
    public String studentComments(@PathVariable Long studentId,
                                  @RequestParam(required = false) Long courseId,
                                  Authentication auth,
                                  Model model) {
        User teacher = userService.findByEmail(auth.getName());
        User student = userService.findById(studentId);

        List<TeacherComment> comments = courseId != null
                ? commentService.getCommentsForStudentInCourse(studentId, courseId)
                : commentService.getVisibleCommentsForStudent(studentId);

        List<Cours> teacherCourses = coursService.getCoursesByTeacher(teacher);

        model.addAttribute("student", student);
        model.addAttribute("teacher", teacher);
        model.addAttribute("comments", comments);
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("tags", commentService.getAllTags());
        model.addAttribute("commentCount", commentService.countForStudent(studentId));

        return "teacher/teacher-student-comments";
    }

    // ========================================
    // TEACHER : Ajouter un commentaire
    // POST /teacher/students/{studentId}/comments
    // ========================================

    @PostMapping("/teacher/students/{studentId}/comments")
    public String addComment(@PathVariable Long studentId,
                             @RequestParam Long courseId,
                             @RequestParam String content,
                             @RequestParam(required = false) String tag,
                             @RequestParam(defaultValue = "TEACHER_ADMIN") String visibility,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User teacher = userService.findByEmail(auth.getName());

        if (content == null || content.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Le commentaire ne peut pas être vide.");
            return "redirect:/teacher/students/" + studentId + "/comments?courseId=" + courseId;
        }
        if (content.length() > 500) {
            redirectAttributes.addFlashAttribute("error", "Commentaire trop long (max 500 caractères).");
            return "redirect:/teacher/students/" + studentId + "/comments?courseId=" + courseId;
        }

        commentService.addComment(teacher.getId(), studentId, courseId, content, tag, visibility);
        redirectAttributes.addFlashAttribute("success", "Commentaire ajouté avec succès.");
        return "redirect:/teacher/students/" + studentId + "/comments?courseId=" + courseId;
    }

    // ========================================
    // TEACHER : Épingler / désépingler
    // POST /teacher/comments/{id}/pin
    // ========================================

    @PostMapping("/teacher/comments/{id}/pin")
    public String togglePin(@PathVariable Long id,
                            @RequestParam Long studentId,
                            @RequestParam(required = false) Long courseId,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        User teacher = userService.findByEmail(auth.getName());
        try {
            commentService.togglePin(id, teacher.getId());
            redirectAttributes.addFlashAttribute("success", "Commentaire mis à jour.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = "redirect:/teacher/students/" + studentId + "/comments";
        if (courseId != null) redirect += "?courseId=" + courseId;
        return redirect;
    }

    // ========================================
    // TEACHER : Modifier un commentaire
    // POST /teacher/comments/{id}/edit
    // ========================================

    @PostMapping("/teacher/comments/{id}/edit")
    public String editComment(@PathVariable Long id,
                              @RequestParam Long studentId,
                              @RequestParam(required = false) Long courseId,
                              @RequestParam String content,
                              @RequestParam(required = false) String tag,
                              @RequestParam(defaultValue = "TEACHER_ADMIN") String visibility,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        User teacher = userService.findByEmail(auth.getName());
        try {
            commentService.updateComment(id, teacher.getId(), content, tag, visibility);
            redirectAttributes.addFlashAttribute("success", "Commentaire modifié.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = "redirect:/teacher/students/" + studentId + "/comments";
        if (courseId != null) redirect += "?courseId=" + courseId;
        return redirect;
    }

    // ========================================
    // TEACHER : Supprimer un commentaire
    // POST /teacher/comments/{id}/delete
    // ========================================

    @PostMapping("/teacher/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @RequestParam Long studentId,
                                @RequestParam(required = false) Long courseId,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        User teacher = userService.findByEmail(auth.getName());
        try {
            commentService.deleteComment(id, teacher.getId(), false);
            redirectAttributes.addFlashAttribute("success", "Commentaire supprimé.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = "redirect:/teacher/students/" + studentId + "/comments";
        if (courseId != null) redirect += "?courseId=" + courseId;
        return redirect;
    }

    // ========================================
    // ADMIN : Vue globale de tous les commentaires
    // GET /admin/comments
    // ========================================

    @GetMapping("/admin/comments")
    public String adminComments(Authentication auth, Model model) {
        List<TeacherComment> allComments = commentService.getAllComments();
        model.addAttribute("comments", allComments);
        model.addAttribute("totalComments", allComments.size());
        return "admin/admin-comments";
    }

    // ========================================
    // ADMIN : Supprimer un commentaire
    // POST /admin/comments/{id}/delete
    // ========================================

    @PostMapping("/admin/comments/{id}/delete")
    public String adminDeleteComment(@PathVariable Long id,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        User admin = userService.findByEmail(auth.getName());
        try {
            commentService.deleteComment(id, admin.getId(), true);
            redirectAttributes.addFlashAttribute("success", "Commentaire supprimé par l'administration.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/comments";
    }
}