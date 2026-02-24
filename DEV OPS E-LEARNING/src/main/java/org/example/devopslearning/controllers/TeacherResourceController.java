package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.UserService;
import org.example.devopslearning.services.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/resources")
@RequiredArgsConstructor
public class TeacherResourceController {

    private final CoursService coursService;
    private final UserService userService;
    private final S3StorageService s3StorageService;

    // ========================================
    // 📄 PAGES WEB
    // ========================================

    /**
     * ✅ Page principale de gestion des ressources d'un cours
     */
    @GetMapping
    public String listResources(
            @RequestParam(required = false) Long courseId,
            Authentication auth,
            Model model) {

        User teacher = userService.findByEmail(auth.getName());

        // Si courseId est fourni, on récupère les ressources de ce cours
        if (courseId != null) {
            Cours cours = coursService.getById(courseId);

            // Vérifier que l'enseignant est bien le propriétaire
            if (!coursService.isTeacherOwner(courseId, teacher)) {
                return "redirect:/teacher/courses?error=unauthorized";
            }

            List<CourseRessource> resources = coursService.getResources(courseId);

            model.addAttribute("cours", cours);
            model.addAttribute("resources", resources);
            model.addAttribute("selectedCourseId", courseId);
        }

        // Liste de tous les cours de l'enseignant (pour le sélecteur)
        model.addAttribute("courses", coursService.getCoursesByTeacher(teacher));

        return "courses/teacher-resources";
    }

    /**
     * ✅ Modal/Page d'upload de ressource
     */
    @GetMapping("/upload")
    public String uploadForm(
            @RequestParam Long courseId,
            Model model) {

        Cours cours = coursService.getById(courseId);
        model.addAttribute("cours", cours);

        return "courses/resource-upload-modal";
    }

    // ========================================
    // 🔄 ACTIONS (POST)
    // ========================================

    /**
     * ✅ Upload d'une ressource (tous types de fichiers)
     */
    @PostMapping("/upload")
    public String uploadResource(
            @RequestParam Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            User teacher = userService.findByEmail(auth.getName());

            // Vérifier que l'enseignant est propriétaire du cours
            if (!coursService.isTeacherOwner(courseId, teacher)) {
                ra.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce cours");
                return "redirect:/teacher/courses";
            }

            // Validation du fichier
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner un fichier");
                return "redirect:/teacher/resources?courseId=" + courseId;
            }

            // Déterminer le type de fichier et le dossier S3
            String folderPrefix = determineFolder(file.getOriginalFilename());

            // Upload vers S3
            String fileUrl = s3StorageService.uploadFileInFolder(file, folderPrefix);

            // Créer la ressource en base
            CourseRessource resource = coursService.addResourceToCourse(
                    courseId,
                    title,
                    fileUrl
            );

            // Ajouter la description si fournie
            if (description != null && !description.trim().isEmpty()) {
                coursService.updateResource(resource.getId(), title, description, fileUrl);
            }

            ra.addFlashAttribute("success", "Ressource '" + title + "' ajoutée avec succès");

        } catch (IOException e) {
            ra.addFlashAttribute("error", "Erreur lors de l'upload : " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }

        return "redirect:/teacher/resources?courseId=" + courseId;
    }

    /**
     * ✅ Modifier une ressource (titre et description)
     */
    @PostMapping("/{resourceId}/edit")
    public String editResource(
            @PathVariable Long resourceId,
            @RequestParam Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            User teacher = userService.findByEmail(auth.getName());

            if (!coursService.isTeacherOwner(courseId, teacher)) {
                ra.addFlashAttribute("error", "Non autorisé");
                return "redirect:/teacher/courses";
            }

            CourseRessource resource = coursService.getResourceById(resourceId);
            coursService.updateResource(resourceId, title, description, resource.getUrl());

            ra.addFlashAttribute("success", "Ressource modifiée avec succès");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }

        return "redirect:/teacher/resources?courseId=" + courseId;
    }

    /**
     * ✅ Supprimer une ressource
     */
    @PostMapping("/{resourceId}/delete")
    public String deleteResource(
            @PathVariable Long resourceId,
            @RequestParam Long courseId,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            User teacher = userService.findByEmail(auth.getName());

            if (!coursService.isTeacherOwner(courseId, teacher)) {
                ra.addFlashAttribute("error", "Non autorisé");
                return "redirect:/teacher/courses";
            }

            coursService.deleteResource(resourceId);
            ra.addFlashAttribute("success", "Ressource supprimée avec succès");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }

        return "redirect:/teacher/resources?courseId=" + courseId;
    }

    // ========================================
    // 🔌 API REST (pour AJAX)
    // ========================================

    /**
     * ✅ API : Liste des ressources d'un cours (JSON)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResourcesApi(
            @RequestParam Long courseId,
            Authentication auth) {

        try {
            User teacher = userService.findByEmail(auth.getName());

            if (!coursService.isTeacherOwner(courseId, teacher)) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "Non autorisé")
                );
            }

            List<CourseRessource> resources = coursService.getResources(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resources", resources);
            response.put("count", resources.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * ✅ API : Détails d'une ressource (JSON)
     */
    @GetMapping("/api/{resourceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResourceDetailsApi(
            @PathVariable Long resourceId,
            Authentication auth) {

        try {
            CourseRessource resource = coursService.getResourceById(resourceId);
            Long courseId = resource.getCourse().getId();

            User teacher = userService.findByEmail(auth.getName());

            if (!coursService.isTeacherOwner(courseId, teacher)) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "Non autorisé")
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resource", resource);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * ✅ API : Statistiques des ressources
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResourceStatsApi(
            @RequestParam Long courseId,
            Authentication auth) {

        try {
            User teacher = userService.findByEmail(auth.getName());

            if (!coursService.isTeacherOwner(courseId, teacher)) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "Non autorisé")
                );
            }

            List<CourseRessource> resources = coursService.getResources(courseId);

            // Compter par type
            Map<String, Long> typeCount = new HashMap<>();
            for (CourseRessource r : resources) {
                String type = r.getType() != null ? r.getType() : "OTHER";
                typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalResources", resources.size());
            response.put("typeCount", typeCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // ========================================
    // 🛠️ MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Détermine le dossier S3 en fonction du type de fichier
     */
    private String determineFolder(String filename) {
        if (filename == null) return "uploads/other";

        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) return "uploads/documents";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "uploads/documents";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "uploads/presentations";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "uploads/spreadsheets";
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")) return "uploads/audio";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "uploads/video";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) return "uploads/images";
        if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return "uploads/archives";

        return "uploads/other";
    }

    /**
     * Détermine l'icône Bootstrap en fonction du type de fichier
     */
    private String getFileIcon(String type) {
        if (type == null) return "bi-file-earmark";

        return switch (type.toUpperCase()) {
            case "PDF" -> "bi-file-earmark-pdf";
            case "WORD" -> "bi-file-earmark-word";
            case "PPT" -> "bi-file-earmark-slides";
            case "EXCEL" -> "bi-file-earmark-excel";
            case "IMAGE" -> "bi-file-earmark-image";
            case "VIDEO" -> "bi-camera-video";
            case "AUDIO" -> "bi-file-earmark-music";
            case "ARCHIVE" -> "bi-file-earmark-zip";
            default -> "bi-file-earmark";
        };
    }
}