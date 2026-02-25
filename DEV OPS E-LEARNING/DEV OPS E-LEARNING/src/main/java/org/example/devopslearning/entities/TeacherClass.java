package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * ✅ ENTITÉ DE LIAISON : Enseignant ↔ Classe
 */
@Getter
@Setter
@Entity
@Table(name = "teacher_classes")
public class TeacherClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "classe_id", nullable = false)
    private AcademicClass classe;

    @Column(name = "date_affectation", nullable = false)
    private Instant dateAffectation;

    @PrePersist
    protected void onCreate() {
        if (dateAffectation == null) {
            dateAffectation = Instant.now();
        }
    }
}