package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region:eu-north-1}")
    private String region;

    // ------------------------------------------------------------
    // ✅ MÉTHODE AMÉLIORÉE : Retourne URL HTTPS directement
    // ------------------------------------------------------------
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String key = "uploads/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        // ✅ Retourner l'URL HTTPS au lieu de s3://
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, key);
    }

    // ------------------------------------------------------------
    // ✅ Upload avec dossier personnalisé
    // ------------------------------------------------------------
    public String uploadFileInFolder(MultipartFile file, String folderPrefix) throws IOException {
        String originalName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;
        String key = folderPrefix + "/" + uniqueFileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, key);
    }

    // ------------------------------------------------------------
    // ✅ NOUVELLE : Convertir s3:// en URL HTTPS
    // ------------------------------------------------------------
    public String convertS3ToHttpsUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }

        // Si c'est déjà une URL HTTPS, la retourner telle quelle
        if (s3Url.startsWith("https://")) {
            return s3Url;
        }

        // Convertir s3:// en HTTPS
        if (s3Url.startsWith("s3://")) {
            String prefix = "s3://" + bucketName + "/";
            String key = s3Url.replace(prefix, "");
            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, key);
        }

        return s3Url;
    }

    // ------------------------------------------------------------
    // ✅ AMÉLIORÉE : URL pré-signée qui FORCE le téléchargement
    // ------------------------------------------------------------
    public URL generatePresignedDownloadUrl(String s3Url, String fileName) {
        String key = extractKeyFromUrl(s3Url);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .responseContentDisposition("attachment; filename=\"" + fileName + "\"") // ✅ Force téléchargement
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    // ------------------------------------------------------------
    // ✅ ANCIENNE MÉTHODE : URL pré-signée simple (ouvre dans navigateur)
    // ------------------------------------------------------------
    public URL generatePresignedUrlFromS3Url(String s3Url) {
        String key = extractKeyFromUrl(s3Url);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    // ------------------------------------------------------------
    // ✅ UTILITAIRE : Extraire la clé S3 depuis n'importe quel format d'URL
    // ------------------------------------------------------------
    private String extractKeyFromUrl(String url) {
        if (url.startsWith("s3://")) {
            String prefix = "s3://" + bucketName + "/";
            return url.replace(prefix, "");
        } else if (url.startsWith("https://")) {
            // Format: https://bucket.s3.region.amazonaws.com/key
            String pattern = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            return url.replace(pattern, "");
        }
        return url;
    }

    // ------------------------------------------------------------
    // ✅ UTILITAIRE : Extraire le nom du fichier depuis l'URL
    // ------------------------------------------------------------
    public String extractFileNameFromUrl(String url) {
        String key = extractKeyFromUrl(url);
        String[] parts = key.split("/");
        String fileName = parts[parts.length - 1];

        // Retirer l'UUID du début (format: uuid_filename.ext)
        if (fileName.contains("_")) {
            int firstUnderscore = fileName.indexOf("_");
            return fileName.substring(firstUnderscore + 1);
        }

        return fileName;
    }
}
