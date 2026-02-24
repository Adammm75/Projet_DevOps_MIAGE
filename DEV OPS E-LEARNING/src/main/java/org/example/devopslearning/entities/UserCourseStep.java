package org.example.devopslearning.entities;

import java.time.Instant;

import org.example.devopslearning.enums.StepName;
import org.example.devopslearning.enums.StepStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_course_steps",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","course_id","step_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long courseId;

    @Enumerated(EnumType.STRING)
    private StepName stepName;

    @Enumerated(EnumType.STRING)
    private StepStatus status;

    private Instant lastUpdated;
}
