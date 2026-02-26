package org.example.devopslearning.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "qcms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Qcm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cours_id", nullable = false)
    @JsonIgnoreProperties({"resources", "students", "createdBy"})
    private Cours cours;

    @Size(max = 255)
    @NotNull
    @Column(name = "titre", nullable = false)
    private String titre;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "publie", nullable = false)
    private Boolean publie = false;

    @Column(name = "duree_minutes")
    private Integer limiteTempsMinutes;

    @Column(name = "tentatives_max")
    private Integer tentativesMax;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"password", "roles"})
    private User creeePar;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant dateCreation;

    @OneToMany(mappedBy = "qcm", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @JsonIgnoreProperties("qcm")
    private List<QcmQuestion> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
        if (publie == null) {
            publie = false;
        }
    }

    public void addQuestion(QcmQuestion question) {
        questions.add(question);
        question.setQcm(this);
    }

    public void removeQuestion(QcmQuestion question) {
        questions.remove(question);
        question.setQcm(null);
    }

    public int getNombreQuestions() {
        return questions != null ? questions.size() : 0;
    }

    public boolean hasTimeLimit() {
        return limiteTempsMinutes != null && limiteTempsMinutes > 0;
    }

    public boolean hasAttemptLimit() {
        return tentativesMax != null && tentativesMax > 0;
    }
}