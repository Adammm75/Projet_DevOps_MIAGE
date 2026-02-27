package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {
    List<TeacherClass> findByTeacherId(Long teacherId);

    List<TeacherClass> findByClasseId(Long classeId);

    boolean existsByTeacherIdAndClasseId(Long teacherId, Long classeId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByTeacherIdAndClasseId(Long teacherId, Long classeId);

    @org.springframework.data.jpa.repository.Query("SELECT tc.teacher.id FROM TeacherClass tc WHERE tc.classe.id = :classeId")
    List<Long> findTeacherIdsByClasseId(Long classeId);
}