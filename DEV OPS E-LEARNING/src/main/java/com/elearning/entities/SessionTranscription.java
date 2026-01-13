package com.elearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "session_transcriptions")
public class SessionTranscription {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private CourseSession session;

    @Size(max = 500)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Size(max = 500)
    @Column(name = "transcript_url", length = 500)
    private String transcriptUrl;

    @Lob
    @Column(name = "transcript_text")
    private String transcriptText;

    @Lob
    @Column(name = "summary_text")
    private String summaryText;

    @Lob
    @Column(name = "keywords")
    private String keywords;

    @Size(max = 100)
    @Column(name = "storage_provider", length = 100)
    private String storageProvider;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}