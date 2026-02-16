package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QcmOptionRepository extends JpaRepository<QcmOption, Long> {
    List<QcmOption> findByQuestionId(Long questionId);
}
