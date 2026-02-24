package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "parcours")
public class Parcour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AJOUTÉ
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "niveau_id", nullable = false)
    private NiveauxEtude niveau;

    @Size(max = 150)
    @NotNull
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Lob
    @Column(name = "description")
    private String description;

}