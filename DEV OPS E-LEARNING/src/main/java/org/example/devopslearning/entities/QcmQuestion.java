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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "qcm_questions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QcmQuestion {

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
    @Lob
    @Column(name = "texte_question", nullable = false)
    private String texteQuestion;

    @NotNull
    @ColumnDefault("'CHOIX_SIMPLE'")
    @Lob
    @Column(name = "type_question", nullable = false)
    private String typeQuestion;

    @NotNull
    @ColumnDefault("1.00")
    @Column(name = "points", nullable = false, precision = 5, scale = 2)
    private BigDecimal points;

    @NotNull
    @Column(name = "position", nullable = false)
    private Integer position;

    // ✅ Relation OneToMany avec les options
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @JsonIgnoreProperties("question")
    private List<QcmOption> options = new ArrayList<>();

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Ajoute une option à la question
     */
    public void addOption(QcmOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    /**
     * Supprime une option de la question
     */
    public void removeOption(QcmOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    /**
     * Vérifie si la question a au moins une bonne réponse
     */
    public boolean hasCorrectAnswer() {
        return options.stream().anyMatch(QcmOption::getEstCorrecte);
    }

    /**
     * Compte le nombre de bonnes réponses
     */
    public long countCorrectAnswers() {
        return options.stream().filter(QcmOption::getEstCorrecte).count();
    }
}