package org.example.devopslearning.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "qcm_tentatives")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QcmTentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AUTO_INCREMENT
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "qcm_id", nullable = false)
    @JsonIgnoreProperties({"questions", "cours", "creeePar"})
    private Qcm qcm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "etudiant_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles"})
    private User etudiant;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin")
    private Instant dateFin;

    @NotNull
    @ColumnDefault("'EN_COURS'")
    @Lob
    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "score_max", precision = 5, scale = 2)
    private BigDecimal scoreMax;

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    @PrePersist
    protected void onCreate() {
        if (dateDebut == null) {
            dateDebut = Instant.now();
        }
        if (statut == null) {
            statut = "EN_COURS";
        }
    }

    /**
     * Calcule la durée de la tentative en minutes
     */
    public Long getDureeMinutes() {
        if (dateDebut == null || dateFin == null) {
            return null;
        }
        return java.time.Duration.between(dateDebut, dateFin).toMinutes();
    }

    /**
     * Calcule le pourcentage de réussite
     */
    public Double getPourcentage() {
        if (score == null || scoreMax == null || scoreMax.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return score.divide(scoreMax, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Vérifie si la tentative est réussie (>= 50%)
     */
    public Boolean isReussie() {
        Double pourcentage = getPourcentage();
        return pourcentage != null && pourcentage >= 50.0;
    }
}