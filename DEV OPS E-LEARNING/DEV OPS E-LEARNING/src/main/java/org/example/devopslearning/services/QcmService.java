package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ SERVICE QCM COMPLET - Gestion complète des QCM
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QcmService {

    private final QcmRepository qcmRepository;
    private final QcmQuestionRepository questionRepository;
    private final QcmOptionRepository optionRepository;
    private final QcmTentativeRepository tentativeRepository;
    private final QcmReponsRepository reponseRepository;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;

    // ========================================
    // GESTION DES QCM (CRUD)
    // ========================================

    /**
     * Crée un nouveau QCM
     */
    public Qcm createQcm(Qcm qcm, Long enseignantId) {
        User enseignant = userRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));

        qcm.setCreeePar(enseignant);
        qcm.setDateCreation(Instant.now());

        return qcmRepository.save(qcm);
    }

    /**
     * Met à jour un QCM existant
     */
    public Qcm updateQcm(Long qcmId, Qcm qcmData) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        qcm.setTitre(qcmData.getTitre());
        qcm.setDescription(qcmData.getDescription());
        qcm.setLimiteTempsMinutes(qcmData.getLimiteTempsMinutes());
        qcm.setTentativesMax(qcmData.getTentativesMax());
        qcm.setPublie(qcmData.getPublie());
        qcm.setDateMiseAJour(Instant.now());

        return qcmRepository.save(qcm);
    }

    /**
     * Supprime un QCM
     */
    public void deleteQcm(Long qcmId) {
        qcmRepository.deleteById(qcmId);
    }

    /**
     * Publie ou dépublie un QCM
     */
    public Qcm togglePublish(Long qcmId) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        qcm.setPublie(!qcm.getPublie());
        qcm.setDateMiseAJour(Instant.now());

        return qcmRepository.save(qcm);
    }

    /**
     * Duplique un QCM avec toutes ses questions
     */
    public Qcm duplicateQcm(Long qcmId, Long enseignantId) {
        Qcm original = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        User enseignant = userRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));

        // Créer la copie du QCM
        Qcm copie = new Qcm();
        copie.setCours(original.getCours());
        copie.setTitre(original.getTitre() + " (Copie)");
        copie.setDescription(original.getDescription());
        copie.setLimiteTempsMinutes(original.getLimiteTempsMinutes());
        copie.setTentativesMax(original.getTentativesMax());
        copie.setPublie(false);  // Toujours en brouillon
        copie.setCreeePar(enseignant);
        copie.setDateCreation(Instant.now());

        copie = qcmRepository.save(copie);

        // Copier les questions
        List<QcmQuestion> questions = questionRepository.findByQcmIdOrderByPositionAsc(qcmId);
        for (QcmQuestion q : questions) {
            QcmQuestion nouvelleQuestion = new QcmQuestion();
            nouvelleQuestion.setQcm(copie);
            nouvelleQuestion.setTexteQuestion(q.getTexteQuestion());
            nouvelleQuestion.setTypeQuestion(q.getTypeQuestion());
            nouvelleQuestion.setPoints(q.getPoints());
            nouvelleQuestion.setPosition(q.getPosition());

            nouvelleQuestion = questionRepository.save(nouvelleQuestion);

            // Copier les options
            List<QcmOption> options = optionRepository.findByQuestionId(q.getId());
            for (QcmOption o : options) {
                QcmOption nouvelleOption = new QcmOption();
                nouvelleOption.setQuestion(nouvelleQuestion);
                nouvelleOption.setTexteOption(o.getTexteOption());
                nouvelleOption.setEstCorrecte(o.getEstCorrecte());

                optionRepository.save(nouvelleOption);
            }
        }

        return copie;
    }

    // ========================================
    // GESTION DES QUESTIONS
    // ========================================

    /**
     * Ajoute une question à un QCM
     */
    public QcmQuestion addQuestion(Long qcmId, QcmQuestion question) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        question.setQcm(qcm);

        // Déterminer la position
        List<QcmQuestion> existingQuestions = questionRepository.findByQcmIdOrderByPositionAsc(qcmId);
        int nextPosition = existingQuestions.isEmpty() ? 1 :
                existingQuestions.get(existingQuestions.size() - 1).getPosition() + 1;
        question.setPosition(nextPosition);

        return questionRepository.save(question);
    }

    /**
     * Met à jour une question
     */
    public QcmQuestion updateQuestion(Long questionId, QcmQuestion questionData) {
        QcmQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question introuvable"));

        question.setTexteQuestion(questionData.getTexteQuestion());
        question.setTypeQuestion(questionData.getTypeQuestion());
        question.setPoints(questionData.getPoints());

        return questionRepository.save(question);
    }

    /**
     * Supprime une question
     */
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    /**
     * Réordonne les questions d'un QCM
     */
    public void reorderQuestions(Long qcmId, List<Long> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            QcmQuestion question = questionRepository.findById(questionIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Question introuvable"));
            question.setPosition(i + 1);
            questionRepository.save(question);
        }
    }

    // ========================================
    // GESTION DES OPTIONS
    // ========================================

    /**
     * Ajoute une option à une question
     */
    public QcmOption addOption(Long questionId, QcmOption option) {
        QcmQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question introuvable"));

        option.setQuestion(question);
        return optionRepository.save(option);
    }

    /**
     * Supprime une option
     */
    public void deleteOption(Long optionId) {
        optionRepository.deleteById(optionId);
    }

    // ========================================
    // RÉCUPÉRATION DES DONNÉES
    // ========================================

    /**
     * Récupère un QCM par ID
     */
    public Qcm getQcmById(Long qcmId) {
        return qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));
    }

    /**
     * Liste tous les QCM d'un enseignant
     */
    public List<Qcm> getQcmsByEnseignant(Long enseignantId) {
        return qcmRepository.findByCreeeParIdOrderByDateCreationDesc(enseignantId);
    }

    /**
     * Liste les QCM d'un cours pour un enseignant
     */
    public List<Qcm> getQcmsByCourseAndEnseignant(Long coursId, Long enseignantId) {
        return qcmRepository.findByCoursIdAndCreeeParIdOrderByDateCreationDesc(coursId, enseignantId);
    }

    /**
     * Liste les QCM publiés d'un cours
     */
    public List<Qcm> getPublishedQcmsByCourse(Long coursId) {
        return qcmRepository.findByCoursIdAndPublieTrue(coursId);
    }

    /**
     * Récupère les questions d'un QCM
     */
    public List<QcmQuestion> getQuestionsByQcm(Long qcmId) {
        return questionRepository.findByQcmIdOrderByPositionAsc(qcmId);
    }

    /**
     * Récupère les options d'une question
     */
    public List<QcmOption> getOptionsByQuestion(Long questionId) {
        return optionRepository.findByQuestionIdOrderById(questionId);
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Compte le nombre de questions d'un QCM
     */
    public long countQuestions(Long qcmId) {
        return questionRepository.findByQcmId(qcmId).size();
    }

    /**
     * Calcule le score maximum d'un QCM
     */
    public BigDecimal calculateMaxScore(Long qcmId) {
        List<QcmQuestion> questions = questionRepository.findByQcmId(qcmId);
        return questions.stream()
                .map(QcmQuestion::getPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Compte le nombre de tentatives pour un QCM
     */
    public long countTentatives(Long qcmId) {
        return tentativeRepository.findAll().stream()
                .filter(t -> t.getQcm().getId().equals(qcmId))
                .count();
    }

    /**
     * Calcule le score moyen pour un QCM
     */
    public Double calculateAverageScore(Long qcmId) {
        List<QcmTentative> tentatives = tentativeRepository.findAll().stream()
                .filter(t -> t.getQcm().getId().equals(qcmId))
                .filter(t -> t.getScore() != null)
                .collect(Collectors.toList());

        if (tentatives.isEmpty()) {
            return null;
        }

        double sum = tentatives.stream()
                .mapToDouble(t -> t.getScore().doubleValue())
                .sum();

        return sum / tentatives.size();
    }

    /**
     * Récupère les statistiques détaillées d'un QCM
     */
    public Map<String, Object> getQcmStatistics(Long qcmId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("nbQuestions", countQuestions(qcmId));
        stats.put("scoreMax", calculateMaxScore(qcmId));
        stats.put("nbTentatives", countTentatives(qcmId));
        stats.put("scoreMoyen", calculateAverageScore(qcmId));

        return stats;
    }

    // ========================================
    // PASSAGE DE QCM (ÉTUDIANTS)
    // ========================================

    /**
     * Démarre une tentative de QCM
     */
    public QcmTentative startTentative(Long qcmId, Long etudiantId) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        User etudiant = userRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        // Vérifier le nombre de tentatives
        List<QcmTentative> tentatives = tentativeRepository
                .findByQcmIdAndEtudiantIdOrderByDateDebutDesc(qcmId, etudiantId);

        if (qcm.getTentativesMax() != null && tentatives.size() >= qcm.getTentativesMax()) {
            throw new RuntimeException("Nombre maximum de tentatives atteint");
        }

        QcmTentative tentative = new QcmTentative();
        tentative.setQcm(qcm);
        tentative.setEtudiant(etudiant);
        tentative.setDateDebut(Instant.now());
        tentative.setStatut("EN_COURS");
        tentative.setScoreMax(calculateMaxScore(qcmId));

        return tentativeRepository.save(tentative);
    }

    /**
     * Enregistre une réponse
     */
    public void saveReponse(Long tentativeId, Long questionId, Long optionId) {
        QcmTentative tentative = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        QcmQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question introuvable"));

        QcmOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option introuvable"));

        QcmRepons reponse = new QcmRepons();
        reponse.setTentative(tentative);
        reponse.setQuestion(question);
        reponse.setOptionChoisie(option);
        reponse.setEstCorrecte(option.getEstCorrecte());
        reponse.setPointsObtenus(option.getEstCorrecte() ? question.getPoints() : BigDecimal.ZERO);

        reponseRepository.save(reponse);
    }

    /**
     * Termine une tentative et calcule le score
     */
    public QcmTentative finishTentative(Long tentativeId) {
        QcmTentative tentative = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        tentative.setDateFin(Instant.now());
        tentative.setStatut("TERMINE");

        // Calculer le score
        List<QcmRepons> reponses = reponseRepository.findByTentativeId(tentativeId);
        BigDecimal score = reponses.stream()
                .map(QcmRepons::getPointsObtenus)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tentative.setScore(score);

        return tentativeRepository.save(tentative);
    }

    // ========================================
// ⭐ AJOUTER CES MÉTHODES DANS QcmService.java
// ========================================

    // Ajouter cette injection dans les dépendances :
    private final QcmClassRepository qcmClassRepository;
    private final AcademicClassRepository academicClassRepository;

// ========================================
// GESTION DES AFFECTATIONS AUX CLASSES
// ========================================

    /**
     * Affecte un QCM à une classe
     */
    public QcmClass affecterQcmAClasse(Long qcmId, Long classeId) {
        Qcm qcm = qcmRepository.findById(qcmId)
                .orElseThrow(() -> new RuntimeException("QCM introuvable"));

        AcademicClass classe = academicClassRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        // Vérifier si déjà affecté
        if (qcmClassRepository.existsByQcmIdAndClasseId(qcmId, classeId)) {
            throw new RuntimeException("Ce QCM est déjà affecté à cette classe");
        }

        QcmClass qcmClass = new QcmClass();
        qcmClass.setQcm(qcm);
        qcmClass.setClasse(classe);
        qcmClass.setDateAffectation(Instant.now());

        return qcmClassRepository.save(qcmClass);
    }

    /**
     * Affecte un QCM à plusieurs classes
     */
    public void affecterQcmAClasses(Long qcmId, List<Long> classeIds) {
        if (classeIds == null || classeIds.isEmpty()) {
            return;
        }

        for (Long classeId : classeIds) {
            try {
                affecterQcmAClasse(qcmId, classeId);
            } catch (RuntimeException e) {
                // Si déjà affecté, continuer
                if (!e.getMessage().contains("déjà affecté")) {
                    throw e;
                }
            }
        }
    }

    /**
     * Retire un QCM d'une classe
     */
    @Transactional
    public void retirerQcmDeClasse(Long qcmId, Long classeId) {
        qcmClassRepository.deleteByQcmIdAndClasseId(qcmId, classeId);
    }

    /**
     * Récupère les classes auxquelles un QCM est affecté
     */
    public List<QcmClass> getClassesByQcm(Long qcmId) {
        return qcmClassRepository.findByQcmId(qcmId);
    }

    /**
     * Récupère les classes disponibles (non affectées) pour un QCM
     */
    public List<AcademicClass> getClassesDisponibles(Long qcmId) {
        List<Long> assignedClasseIds = qcmClassRepository.findClasseIdsByQcmId(qcmId);

        return academicClassRepository.findAll().stream()
                .filter(c -> !assignedClasseIds.contains(c.getId()))
                .toList();
    }

    /**
     * Récupère les QCM publiés pour une classe (pour les étudiants)
     */
    public List<QcmClass> getPublishedQcmsForClasse(Long classeId) {
        return qcmClassRepository.findPublishedQcmsByClasseId(classeId);
    }

    /**
     * Met à jour les affectations de classes pour un QCM
     */
    @Transactional
    public void updateClassesForQcm(Long qcmId, List<Long> newClasseIds) {
        // Récupérer les IDs actuels
        List<Long> currentIds = qcmClassRepository.findClasseIdsByQcmId(qcmId);

        // Retirer ceux qui ne sont plus sélectionnés
        for (Long currentId : currentIds) {
            if (newClasseIds == null || !newClasseIds.contains(currentId)) {
                retirerQcmDeClasse(qcmId, currentId);
            }
        }

        // Ajouter les nouveaux
        if (newClasseIds != null) {
            affecterQcmAClasses(qcmId, newClasseIds);
        }
    }
}