package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.CoursClass;
import org.example.devopslearning.entities.CoursClassId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursClassRepository extends JpaRepository<CoursClass, CoursClassId> {
    List<CoursClass> findByClasseId(Long classeId);
    List<CoursClass> findByCoursId(Long coursId); // ✅ AJOUTÉ
    boolean existsByCoursIdAndClasseId(Long coursId, Long classeId);
}