package org.example.devopslearning.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entité de liaison entre QCM et Classes
 * Permet d'affecter un QCM à une ou plusieurs classes
 */
@Getter
@Setter
@Entity
@Table(name = "qcm_classes")
public class QcmClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qcm_id", nullable = false)
    @JsonIgnoreProperties({"questions", "cours", "creeePar"})
    private Qcm qcm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classe_id", nullable = false)
    @JsonIgnoreProperties({"parcours", "inscriptions"})
    private AcademicClass classe;

    @Column(name = "date_affectation")
    private Instant dateAffectation;

    // ========================================
    // LIFECYCLE
    // ========================================

    @PrePersist
    protected void onCreate() {
        if (dateAffectation == null) {
            dateAffectation = Instant.now();
        }
    }
}