package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentQcmService {

    private final QcmRepository qcmRepository;
    private final QcmClassRepository qcmClassRepository;
    private final InscriptionsClassRepository inscriptionsClassRepository;
    private final QcmTentativeRepository tentativeRepository;
    private final QcmReponsRepository reponsRepository;
    private final QcmQuestionRepository questionRepository;
    private final QcmOptionRepository optionRepository;

    /**
     * ✅ CORRIGÉ - Gestion des QCM orphelins
     */
    public List<Qcm> getAvailableQcms(Long studentId) {
        List<InscriptionsClass> inscriptions = inscriptionsClassRepository.findByEtudiantId(studentId);
        List<Long> classeIds = inscriptions.stream()
                .map(i -> i.getClasse().getId())
                .collect(Collectors.toList());

        if (classeIds.isEmpty()) {
            return List.of();
        }

        return classeIds.stream()
                .flatMap(cid -> qcmClassRepository.findByClasseId(cid).stream())
                .map(QcmClass::getQcm)
                .filter(qcm -> {
                    try {
                        // ✅ Vérifier que le QCM existe avant d'accéder à ses propriétés
                        return qcm != null && qcm.getPublie();
                    } catch (Exception e) {
                        // ⚠️ QCM orphelin détecté, on l'ignore
                        System.err.println("QCM orphelin détecté et ignoré : " + e.getMessage());
                        return false;
                    }
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Qcm> getQcmsByCourse(Long studentId, Long courseId) {
        return getAvailableQcms(studentId).stream()
                .filter(q -> {
                    try {
                        return q.getCours().getId().equals(courseId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public boolean canAccessQcm(Long studentId, Long qcmId) {
        return getAvailableQcms(studentId).stream()
                .anyMatch(q -> q.getId().equals(qcmId));
    }

    public boolean canStartNewAttempt(Long studentId, Long qcmId) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        if (qcm.getTentativesMax() == null) {
            return true;
        }

        long attempts = tentativeRepository.countByEtudiantIdAndQcmId(studentId, qcmId);
        return attempts < qcm.getTentativesMax();
    }

    @Transactional
    public QcmTentative startAttempt(Long qcmId, Long studentId) {
        QcmTentative t = new QcmTentative();
        t.setQcm(qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable")));

        User etudiant = new User();
        etudiant.setId(studentId);
        t.setEtudiant(etudiant);
        t.setDateDebut(Instant.now());
        t.setStatut("EN_COURS");

        return tentativeRepository.save(t);
    }

    public QcmTentative getAttempt(Long attemptId) {
        return tentativeRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));
    }

    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, Long optionId) {
        QcmRepons r = reponsRepository.findByTentativeIdAndQuestionId(attemptId, questionId)
                .orElse(new QcmRepons());

        r.setTentative(tentativeRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable")));
        r.setQuestion(questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question introuvable")));
        r.setOptionChoisie(optionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option introuvable")));

        reponsRepository.save(r);
    }

    @Transactional
    public void finishAttempt(Long attemptId) {
        QcmTentative t = tentativeRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        t.setDateFin(Instant.now());
        t.setStatut("TERMINE");

        List<QcmRepons> reponses = reponsRepository.findByTentativeId(attemptId);
        BigDecimal score = BigDecimal.ZERO;

        for (QcmRepons r : reponses) {
            if (r.getOptionChoisie() != null && r.getOptionChoisie().getEstCorrecte()) {
                score = score.add(r.getQuestion().getPoints());
            }
        }

        t.setScore(score);
        tentativeRepository.save(t);
    }

    public List<QcmRepons> getResponses(Long attemptId) {
        return reponsRepository.findByTentativeId(attemptId);
    }

    public List<QcmTentative> getAttemptsByQcm(Long studentId, Long qcmId) {
        return tentativeRepository.findByEtudiantIdAndQcmId(studentId, qcmId);
    }

    public List<QcmTentative> getAllAttempts(Long studentId) {
        return tentativeRepository.findByEtudiantId(studentId);
    }
}