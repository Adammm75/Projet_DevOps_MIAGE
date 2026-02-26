package org.example.devopslearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * ✅ DTO pour les informations de ressource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String type;
    private String title;
    private String description;
    private String url;
    private Instant createdAt;

    // Métadonnées additionnelles
    private String fileExtension;
    private String iconClass;
    private String badgeColor;
    private Long fileSize; // Si disponible
}

/**
 * ✅ DTO pour les statistiques de ressources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceStatsDTO {

    private Long courseId;
    private Long totalResources;
    private Map<String, Long> resourcesByType;
    private Long documentsCount;
    private Long audioCount;
    private Long videoCount;
    private Long imagesCount;
    private Long archivesCount;
    private Boolean hasAudio;
    private Boolean hasVideo;
    private Boolean hasDocuments;
}

/**
 * ✅ DTO pour la réponse d'upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceUploadResponse {

    private boolean success;
    private String message;
    private Long resourceId;
    private String resourceUrl;
    private String resourceType;
    private String error;
}

/**
 * ✅ DTO pour la requête de création de ressource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceCreateRequest {

    private Long courseId;
    private String title;
    private String description;
    private String type;
    private String url;
}

/**
 * ✅ DTO pour la requête de mise à jour de ressource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceUpdateRequest {

    private String title;
    private String description;
}

/**
 * ✅ DTO pour la liste de ressources avec pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceListResponse {

    private boolean success;
    private Long totalCount;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private java.util.List<ResourceDTO> resources;
}