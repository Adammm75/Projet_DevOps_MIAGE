package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "qcm_options")
public class QcmOption {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id", nullable = false)
    private QcmQuestion question;

    @NotNull
    @Lob
    @Column(name = "texte_option", nullable = false)
    private String texteOption;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "est_correcte", nullable = false)
    private Boolean estCorrecte = false;

}