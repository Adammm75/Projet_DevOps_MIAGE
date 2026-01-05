package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "qcm_questions")
public class QcmQuestion {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "qcm_id", nullable = false)
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

}