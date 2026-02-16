package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmTentative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QcmTentativeRepository extends JpaRepository<QcmTentative, Long> {
    List<QcmTentative> findByQcmIdAndEtudiantIdOrderByDateDebutDesc(Long qcmId, Long etudiantId);
}
