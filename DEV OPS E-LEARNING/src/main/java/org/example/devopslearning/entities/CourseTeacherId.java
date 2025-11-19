package org.example.devopslearning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class CourseTeacherId implements Serializable {
    private static final long serialVersionUID = 4503806425217899593L;
    @NotNull
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @NotNull
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CourseTeacherId entity = (CourseTeacherId) o;
        return Objects.equals(this.teacherId, entity.teacherId) &&
                Objects.equals(this.courseId, entity.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, courseId);
    }

}