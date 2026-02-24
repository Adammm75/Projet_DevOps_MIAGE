package org.example.devopslearning.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "qcm_reponses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QcmRepons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "tentative_id", nullable = false)
    @JsonIgnoreProperties({"qcm", "etudiant"})
    private QcmTentative tentative;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"qcm", "options"})
    private QcmQuestion question;

    // ⭐ CHANGÉ : option → optionChoisie
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "option_id")
    @JsonIgnoreProperties("question")
    private QcmOption optionChoisie;

    @Lob
    @Column(name = "reponse_texte")
    private String reponseTexte;

    @Column(name = "est_correcte")
    private Boolean estCorrecte;

    @Column(name = "points_obtenus", precision = 5, scale = 2)
    private BigDecimal pointsObtenus;
}