package org.example.devopslearning.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * ✅ ENTITÉ avec génération d'ID AUTO_INCREMENT (IDs courts)
 */
@Getter
@Setter
@Entity
@Table(name = "course_resources")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseRessource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← AUTO_INCREMENT : 1, 2, 3, 4...
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"resources", "createdBy", "students"})
    private Cours course;

    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 500)
    @Column(name = "url", length = 500)
    private String url;

    @Size(max = 100)
    @Column(name = "file_name", length = 100)
    private String fileName;

    @Size(max = 50)
    @Column(name = "content_type", length = 50)
    private String contentType;

    @Size(max = 1000)
    @Column(name = "s3_audio_url", length = 1000)
    private String s3AudioUrl;

    @Lob
    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;

    @Lob
    @Column(name = "summary", columnDefinition = "LONGTEXT")
    private String summary;

    @Lob
    @Column(name = "keywords")
    private String keywords;

    @Lob
    @Column(name = "structure_json")
    private String structureJson;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean hasTranscript() {
        return transcript != null && !transcript.isEmpty();
    }

    public boolean hasSummary() {
        return summary != null && !summary.isEmpty();
    }

    public String getResourceUrl() {
        return s3AudioUrl != null && !s3AudioUrl.isEmpty() ? s3AudioUrl : url;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}