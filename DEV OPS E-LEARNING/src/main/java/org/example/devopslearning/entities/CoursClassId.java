package org.example.devopslearning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

/**
 * ✅ CLÉ COMPOSITE pour CoursClass
 */
@Getter
@Setter
@Embeddable
public class CoursClassId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "cours_id", nullable = false)
    private Long coursId;

    @Column(name = "classe_id", nullable = false)
    private Long classeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CoursClassId entity = (CoursClassId) o;
        return Objects.equals(this.coursId, entity.coursId) &&
                Objects.equals(this.classeId, entity.classeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coursId, classeId);
    }
}