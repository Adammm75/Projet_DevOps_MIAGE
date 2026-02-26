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
    private final CoursClassRepository coursClassRepository; // ✅ AJOUTÉ
    private final QcmTentativeRepository tentativeRepository;
    private final QcmReponsRepository reponsRepository;
    private final QcmQuestionRepository questionRepository;
    private final QcmOptionRepository optionRepository;

    /**
     * ✅ CORRIGÉ - Récupère les QCM disponibles pour un étudiant
     *
     * Logique : étudiant → classes → cours liés à ces classes → QCM publiés de ces cours
     *
     * L'ancienne logique passait par qcm_classes (QCM affecté manuellement à une classe),
     * ce qui échouait si le prof n'avait pas fait cette affectation.
     * La nouvelle logique utilise les cours_classes (cours affecté à une classe),
     * ce qui est le flux normal d'utilisation.
     */
    public List<Qcm> getAvailableQcms(Long studentId) {
        // 1. Récupérer les classes de l'étudiant
        List<InscriptionsClass> inscriptions = inscriptionsClassRepository.findByEtudiantId(studentId);

        System.out.println("🔍 DEBUG - Inscriptions trouvées : " + inscriptions.size() + " pour étudiant " + studentId);

        if (inscriptions.isEmpty()) {
            System.out.println("⚠️ DEBUG - Aucune inscription en classe pour l'étudiant " + studentId);
            return List.of();
        }

        List<Long> classeIds = inscriptions.stream()
                .map(i -> i.getClasse().getId())
                .collect(Collectors.toList());

        System.out.println("🔍 DEBUG - Classes IDs : " + classeIds);

        // 2. Récupérer les cours liés à ces classes (via cours_classes)
        List<Long> coursIds = classeIds.stream()
                .flatMap(cid -> coursClassRepository.findByClasseId(cid).stream())
                .map(cc -> cc.getCours().getId())
                .distinct()
                .collect(Collectors.toList());

        System.out.println("🔍 DEBUG - Cours IDs accessibles : " + coursIds);

        if (coursIds.isEmpty()) {
            System.out.println("⚠️ DEBUG - Aucun cours lié aux classes de l'étudiant");
            return List.of();
        }

        // 3. Récupérer les QCM publiés de ces cours
        List<Qcm> qcms = coursIds.stream()
                .flatMap(coursId -> qcmRepository.findByCoursIdAndPublie(coursId, true).stream())
                .distinct()
                .collect(Collectors.toList());

        System.out.println("🔍 DEBUG - QCM publiés trouvés : " + qcms.size());

        return qcms;
    }

    /**
     * QCM disponibles pour un cours spécifique
     */
    public List<Qcm> getQcmsByCourse(Long studentId, Long courseId) {
        return getAvailableQcms(studentId).stream()
                .filter(q -> q.getCours() != null && q.getCours().getId().equals(courseId))
                .collect(Collectors.toList());
    }

    /**
     * ✅ CORRIGÉ - Vérifie l'accès via les cours (et non plus qcm_classes)
     */
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
        BigDecimal scoreMax = BigDecimal.ZERO;

        for (QcmRepons r : reponses) {
            if (r.getQuestion() != null && r.getQuestion().getPoints() != null) {
                scoreMax = scoreMax.add(r.getQuestion().getPoints());
            }
            if (r.getOptionChoisie() != null && Boolean.TRUE.equals(r.getOptionChoisie().getEstCorrecte())) {
                score = score.add(r.getQuestion().getPoints());
            }
        }

        t.setScore(score);
        t.setScoreMax(scoreMax);
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