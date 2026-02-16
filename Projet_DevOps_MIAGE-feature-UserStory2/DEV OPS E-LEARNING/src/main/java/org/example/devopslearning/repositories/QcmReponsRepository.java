package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.QcmRepons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QcmReponsRepository extends JpaRepository<QcmRepons, Long> {
    List<QcmRepons> findByTentativeId(Long tentativeId);
}
