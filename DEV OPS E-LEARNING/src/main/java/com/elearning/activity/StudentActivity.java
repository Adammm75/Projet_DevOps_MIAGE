package com.elearning.activity;

import com.elearning.entities.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;



import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "student_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentActivity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Cours course;


    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;


    @Column(name = "activity_time", nullable = false)
    private LocalDateTime activityTime;


    /*
    * Stored as JSON in MySQL; requires Hibernate dialect that supports JSON or
    * use String if your JDBC/Hibernate doesn't map JsonNode automatically.
    */

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode metadata;


    @PrePersist
    public void prePersist() {
        if (activityTime == null) activityTime = LocalDateTime.now();
    }
}