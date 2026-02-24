package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CoursFiliere;
import org.example.devopslearning.entities.CoursFiliereId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursFiliereRepository extends JpaRepository<CoursFiliere, CoursFiliereId> {
    boolean existsByCoursIdAndFiliereId(Long coursId, Long filiereId);
}