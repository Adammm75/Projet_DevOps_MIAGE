package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "niveaux_etudes")
public class NiveauxEtude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AJOUTÉ
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "libelle", nullable = false, length = 50)
    private String libelle;

}