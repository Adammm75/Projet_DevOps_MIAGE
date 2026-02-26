package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CourseAccessService;
import org.example.devopslearning.services.UserService;
import org.example.devopslearning.services.CoursService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ CONTRÔLEUR DE TÉLÉCHARGEMENT/VISUALISATION DE RESSOURCES
 *
 * Gère le téléchargement et la visualisation des fichiers stockés sur S3
 * avec redirection directe vers les URLs HTTPS
 */
@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
public class CourseResourceController {

    private final CoursService coursService;
    private final CourseAccessService courseAccessService;
    private final UserService userService;

    /**
     * ✅ TÉLÉCHARGER un fichier
     *
     * Récupère l'URL S3 HTTPS et redirige directement
     */
    @GetMapping("/{resourceId}/download")
    public String download(@PathVariable Long resourceId, Authentication auth) {
        try {
            System.out.println("🔽 Téléchargement - ID: " + resourceId);

            // 1. Vérifier l'accès utilisateur
            User user = userService.findByEmail(auth.getName());
            Long courseId = coursService.getCourseIdByResource(resourceId);

            if (courseId != null && courseAccessService.isStudent(user) &&
                    !courseAccessService.canAccessCourse(user.getId(), courseId)) {
                System.err.println("❌ Accès refusé pour l'utilisateur: " + user.getEmail());
                return "redirect:/dashboard?error=access-denied";
            }

            // 2. Récupérer la ressource
            CourseRessource resource = coursService.getResourceById(resourceId);
            System.out.println("✅ Ressource trouvée: " + resource.getTitle());

            // 3. Récupérer l'URL (priorité: url, puis s3AudioUrl)
            String fileUrl = resource.getUrl();
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                fileUrl = resource.getS3AudioUrl();
            }

            // 4. Vérifier que l'URL existe
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                System.err.println("❌ Aucune URL trouvée pour la ressource ID: " + resourceId);
                return "redirect:/teacher/resources?error=no-url";
            }

            System.out.println("📁 URL de téléchargement: " + fileUrl);

            // 5. Redirection DIRECTE vers l'URL S3 HTTPS
            return "redirect:" + fileUrl;

        } catch (Exception e) {
            System.err.println("❌ ERREUR Téléchargement - ID: " + resourceId);
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/teacher/resources?error=download-failed";
        }
    }

    /**
     * ✅ VISUALISER un fichier (ouvre dans le navigateur)
     *
     * Même logique que download
     */
    @GetMapping("/{resourceId}/open")
    public String open(@PathVariable Long resourceId, Authentication auth) {
        try {
            System.out.println("👁️ Visualisation - ID: " + resourceId);

            // 1. Vérifier l'accès utilisateur
            User user = userService.findByEmail(auth.getName());
            Long courseId = coursService.getCourseIdByResource(resourceId);

            if (courseId != null && courseAccessService.isStudent(user) &&
                    !courseAccessService.canAccessCourse(user.getId(), courseId)) {
                System.err.println("❌ Accès refusé pour l'utilisateur: " + user.getEmail());
                return "redirect:/dashboard?error=access-denied";
            }

            // 2. Récupérer la ressource
            CourseRessource resource = coursService.getResourceById(resourceId);
            System.out.println("✅ Ressource trouvée: " + resource.getTitle());

            // 3. Récupérer l'URL
            String fileUrl = resource.getUrl();
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                fileUrl = resource.getS3AudioUrl();
            }

            // 4. Vérifier que l'URL existe
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                System.err.println("❌ Aucune URL trouvée pour la ressource ID: " + resourceId);
                return "redirect:/teacher/resources?error=no-url";
            }

            System.out.println("📁 URL de visualisation: " + fileUrl);

            // 5. Redirection DIRECTE vers l'URL S3 HTTPS
            return "redirect:" + fileUrl;

        } catch (Exception e) {
            System.err.println("❌ ERREUR Visualisation - ID: " + resourceId);
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/teacher/resources?error=open-failed";
        }
    }
}