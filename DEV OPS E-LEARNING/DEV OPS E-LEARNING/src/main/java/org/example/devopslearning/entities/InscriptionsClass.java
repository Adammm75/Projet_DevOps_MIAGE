package org.example.devopslearning.entities;

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
@Table(name = "inscriptions_classes")
public class InscriptionsClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AJOUTÉ
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "classe_id", nullable = false)
    private AcademicClass classe;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_inscription", nullable = false)
    private Instant dateInscription;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIF'")
    @Column(name = "statut", nullable = false, length = 20)
    private String statut;

    // ✅ MÉTHODE HELPER
    @PrePersist
    public void prePersist() {
        if (dateInscription == null) {
            dateInscription = Instant.now();
        }
        if (statut == null) {
            statut = "ACTIF";
        }
    }
}