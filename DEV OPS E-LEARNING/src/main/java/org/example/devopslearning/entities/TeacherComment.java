package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "teacher_comments")
public class TeacherComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "course_id", nullable = false)
    private Cours course;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /** Tag libre ou parmi les suggestions prédéfinies */
    @Size(max = 50)
    @Column(name = "tag", length = 50)
    private String tag;

    /**
     * Visibilité : TEACHER_ADMIN (enseignants + admin) ou ADMIN_ONLY (admin uniquement)
     */
    @Size(max = 20)
    @Column(name = "visibility", length = 20, nullable = false)
    private String visibility = "TEACHER_ADMIN";

    /** Commentaire épinglé en haut du profil étudiant */
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        if (visibility == null) visibility = "TEACHER_ADMIN";
        if (isPinned == null) isPinned = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}