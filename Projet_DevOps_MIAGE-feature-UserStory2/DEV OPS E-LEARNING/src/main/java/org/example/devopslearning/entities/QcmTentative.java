package org.example.devopslearning.entities;

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
public class QcmTentative {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "qcm_id", nullable = false)
    private Qcm qcm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "etudiant_id", nullable = false)
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

}