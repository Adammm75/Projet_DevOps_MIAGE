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
@Table(name = "classes")
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ AJOUTÉ
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parcours_id", nullable = false)
    private Parcour parcours;

    @Size(max = 150)
    @NotNull
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Size(max = 50)
    @Column(name = "code", length = 50)
    private String code;

    @Size(max = 9)
    @NotNull
    @Column(name = "annee_universitaire", nullable = false, length = 9)
    private String anneeUniversitaire;
}