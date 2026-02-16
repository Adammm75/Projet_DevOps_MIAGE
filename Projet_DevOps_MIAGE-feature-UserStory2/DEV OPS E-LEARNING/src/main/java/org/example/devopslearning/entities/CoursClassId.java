package org.example.devopslearning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursClassId implements Serializable {

    @Column(name = "cours_id", nullable = false)
    private Long coursId;

    @Column(name = "classe_id", nullable = false)
    private Long classeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursClassId that = (CoursClassId) o;
        return Objects.equals(coursId, that.coursId) &&
                Objects.equals(classeId, that.classeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coursId, classeId);
    }
}