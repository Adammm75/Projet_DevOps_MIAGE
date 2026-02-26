package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "notes_cours")
public class NotesCour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @Column(name = "note_finale", precision = 5, scale = 2)
    private BigDecimal noteFinale;

    @NotNull
    @ColumnDefault("20.00")
    @Column(name = "note_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal noteMax;

    @Size(max = 5)
    @Column(name = "mention", length = 5)
    private String mention;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'PROVISOIRE'")
    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    @Column(name = "date_calcul")
    private Instant dateCalcul;

    // ✅ AJOUTÉ - Date de création
    @Column(name = "created_at")
    private Instant createdAt;

    // ✅ MÉTHODE HELPER
    @PrePersist
    public void prePersist() {
        if (noteMax == null) {
            noteMax = new BigDecimal("20.00");
        }
        if (statut == null) {
            statut = "PROVISOIRE";
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}