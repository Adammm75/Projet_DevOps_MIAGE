package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QcmQuestionRepository extends JpaRepository<QcmQuestion, Long> {
    List<QcmQuestion> findByQcmIdOrderByPositionAsc(Long qcmId);
    List<QcmQuestion> findByQcmId(Long qcmId);
}
