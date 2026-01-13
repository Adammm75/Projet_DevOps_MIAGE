package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CourseAccessService;
import org.example.devopslearning.services.UserService;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.S3StorageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
public class CourseResourceController {

    private final CoursService coursService;
    private final CourseAccessService courseAccessService;
    private final UserService userService;
    private final S3StorageService s3StorageService; // ✅ Ajouté

    /**
     * ✅ CORRIGÉ : Télécharger une ressource avec URL pré-signée
     * Route: /resources/{resourceId}/download
     * Le fichier sera téléchargé automatiquement (pas ouvert dans le navigateur)
     */
    @GetMapping("/{resourceId}/download")
    public String download(@PathVariable Long resourceId, Authentication auth) {
        User u = userService.findByEmail(auth.getName());
        Long courseId = coursService.getCourseIdByResource(resourceId);

        // Vérifier si l'utilisateur est étudiant et s'il peut accéder au cours
        if (courseId != null && courseAccessService.isStudent(u) &&
                !courseAccessService.canAccessCourse(u.getId(), courseId)) {
            return "redirect:/dashboard?forbidden";
        }

        // ✅ Récupérer l'URL S3 (format s3:// ou https://)
        String s3Url = coursService.getResourceUrl(resourceId);

        // ✅ Extraire le nom du fichier
        String fileName = s3StorageService.extractFileNameFromUrl(s3Url);

        // ✅ Générer une URL pré-signée qui FORCE le téléchargement
        URL downloadUrl = s3StorageService.generatePresignedDownloadUrl(s3Url, fileName);

        // ✅ Rediriger vers l'URL pré-signée
        return "redirect:" + downloadUrl.toString();
    }

    /**
     * ✅ CORRIGÉ : Ouvrir une ressource dans le navigateur (aperçu)
     * Route: /resources/{resourceId}/open
     */
    @GetMapping("/{resourceId}/open")
    public String open(@PathVariable Long resourceId, Authentication auth) {
        User u = userService.findByEmail(auth.getName());
        Long courseId = coursService.getCourseIdByResource(resourceId);

        // Vérifier si l'utilisateur est étudiant et s'il peut accéder au cours
        if (courseId != null && courseAccessService.isStudent(u) &&
                !courseAccessService.canAccessCourse(u.getId(), courseId)) {
            return "redirect:/dashboard?forbidden";
        }

        // ✅ Récupérer l'URL et la convertir en HTTPS si nécessaire
        String s3Url = coursService.getResourceUrl(resourceId);
        String httpsUrl = s3StorageService.convertS3ToHttpsUrl(s3Url);

        return "redirect:" + httpsUrl;
    }
}
