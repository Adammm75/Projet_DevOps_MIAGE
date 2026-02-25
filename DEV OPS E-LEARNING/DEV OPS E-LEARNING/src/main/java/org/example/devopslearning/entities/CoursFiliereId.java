package org.example.devopslearning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class CoursFiliereId implements java.io.Serializable {
    private static final long serialVersionUID = 1993942661138248035L;
    @NotNull
    @Column(name = "cours_id", nullable = false)
    private Long coursId;

    @NotNull
    @Column(name = "filiere_id", nullable = false)
    private Long filiereId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CoursFiliereId entity = (CoursFiliereId) o;
        return Objects.equals(this.filiereId, entity.filiereId) &&
                Objects.equals(this.coursId, entity.coursId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filiereId, coursId);
    }

}