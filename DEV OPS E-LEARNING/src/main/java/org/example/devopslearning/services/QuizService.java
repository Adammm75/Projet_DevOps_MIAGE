package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QcmRepository qcmRepository;
    private final QcmQuestionRepository questionRepository;
    private final QcmOptionRepository optionRepository;
    private final QcmTentativeRepository tentativeRepository;
    private final QcmReponsRepository reponseRepository;
    private final UserRepository userRepository;

    /**
     * Crée un nouveau QCM
     */
    public Qcm createQuiz(Qcm qcm) {
        return qcmRepository.save(qcm);
    }

    /**
     * Liste tous les QCM publiés pour un cours donné
     */
    public List<Qcm> listPublishedByCourse(Long courseId) {
        // Récupère tous les QCM publiés pour ce cours
        return qcmRepository.findByCoursIdAndPublieTrue(courseId);
    }
    /** Démarre une tentative de QCM pour un étudiant
     */
    public QcmTentative startQuiz(Long qcmId, Long studentId) {

        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        QcmTentative t = new QcmTentative();
        t.setQcm(qcm);
        t.setEtudiant(student);
        t.setDateDebut(Instant.now());
        t.setStatut("EN_COURS");

        return tentativeRepository.save(t);
    }

    /**
     * Récupère toutes les questions d'un QCM
     */
    public List<QcmQuestion> getQuestions(Long qcmId) {
        return questionRepository.findByQcmId(qcmId);
    }

    /**
     * Soumet une réponse à une question
     */
    public void submitAnswer(QcmRepons rep) {
        reponseRepository.save(rep);
    }

    /**
     * Termine une tentative de QCM (sans calcul de score)
     */
    public void finishQuiz(Long tentativeId) {
        QcmTentative t = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        t.setDateFin(Instant.now());
        t.setStatut("TERMINE");

        // Le score sera calculé ultérieurement ou par une autre méthode
        tentativeRepository.save(t);
    }

    /**
     * Termine une tentative de QCM avec calcul de score
     */
    public void finishQuiz(Long tentativeId, Double score, Double maxScore) {

        QcmTentative t = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        t.setScore(BigDecimal.valueOf(score));
        t.setScoreMax(BigDecimal.valueOf(maxScore));
        t.setDateFin(Instant.now());
        t.setStatut("TERMINE");

        tentativeRepository.save(t);
    }
}