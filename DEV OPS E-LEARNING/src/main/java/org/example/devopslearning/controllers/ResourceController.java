package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.RessourceCours;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.example.devopslearning.services.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final CourseResourceRepository courseResourceRepository;
    private final CoursRepository coursRepository;
    private final S3StorageService s3StorageService;

    /**
     * 📥 UPLOAD d'un fichier vers AWS S3 puis enregistrement en base de données
     */
    @PostMapping("/courses/{courseId}/upload")
    public ResponseEntity<?> uploadResource(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description
    ) {
        try {
            // 1. Vérifier que le cours existe
            Cours cours = coursRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable : " + courseId));

            // 2. Upload du fichier sur AWS S3 (CORRECTION ICI)
            String s3Url = s3StorageService.uploadFileInFolder(file, "courses/" + courseId);

            // 3. Créer l'objet RessourceCours
            RessourceCours resource = new RessourceCours();
            resource.setCourse(cours);
            resource.setType("FILE");
            resource.setTitle(title != null ? title : file.getOriginalFilename());
            resource.setDescription(description);
            resource.setUrl(s3Url);
            resource.setFileName(file.getOriginalFilename());
            resource.setContentType(file.getContentType());

            // 4. Enregistrer en BDD
            RessourceCours saved = courseResourceRepository.save(resource);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur upload : " + e.getMessage());
        }
    }

    /**
     * 📥 Générer une URL SIGNÉE pour télécharger le fichier depuis S3
     */
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<?> getDownloadUrl(@PathVariable Long resourceId) {

        // 1. Lire la ressource
        RessourceCours res = courseResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable : " + resourceId));

        // 2. Générer un lien S3 temporaire (30 minutes)
        URL presignedUrl = s3StorageService.generatePresignedUrlFromS3Url(res.getUrl());

        // 3. Retourner le lien de téléchargement
        return ResponseEntity.ok(presignedUrl.toString());
    }
}