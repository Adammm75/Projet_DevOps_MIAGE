package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "cours_filieres")
public class CoursFiliere {
    @EmbeddedId
    private CoursFiliereId id;

    @MapsId("coursId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @MapsId("filiereId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

}