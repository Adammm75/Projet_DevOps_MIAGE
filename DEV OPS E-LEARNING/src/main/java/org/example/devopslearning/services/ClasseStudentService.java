package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.AcademicClass;
import org.example.devopslearning.entities.InscriptionsClass;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AcademicClassRepository;
import org.example.devopslearning.repositories.InscriptionsClassRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClasseStudentService {

    private final InscriptionsClassRepository inscriptionsClassRepository;
    private final AcademicClassRepository classeRepository;
    private final UserRepository userRepository;

    /**
     * Récupère tous les étudiants d'une classe
     */
    public List<User> getStudentsInClass(Long classeId) {
        return inscriptionsClassRepository.findByClasseIdAndStatut(classeId, "ACTIF")
                .stream()
                .map(InscriptionsClass::getEtudiant)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les inscriptions d'une classe (avec détails)
     */
    public List<InscriptionsClass> getInscriptionsForClass(Long classeId) {
        return inscriptionsClassRepository.findByClasseId(classeId);
    }

    /**
     * Compte le nombre d'étudiants actifs dans une classe
     */
    public long countActiveStudentsInClass(Long classeId) {
        return inscriptionsClassRepository.countByClasseIdAndStatut(classeId, "ACTIF");
    }

    /**
     * Récupère les étudiants disponibles (non inscrits dans la classe)
     */
    public List<User> getAvailableStudents(Long classeId) {
        return inscriptionsClassRepository.findStudentsNotInClass(classeId);
    }

    /**
     * Inscrit un étudiant dans une classe
     */
    @Transactional
    public InscriptionsClass enrollStudent(Long classeId, Long etudiantId) {
        // Vérifier si déjà inscrit dans CETTE classe
        if (inscriptionsClassRepository.existsByClasseIdAndEtudiantId(classeId, etudiantId)) {
            throw new RuntimeException("L'étudiant est déjà inscrit dans cette classe");
        }

        // ✅ NOUVEAU : Vérifier si l'étudiant a déjà une classe active
        List<InscriptionsClass> activeClasses = inscriptionsClassRepository
                .findByEtudiantIdAndStatut(etudiantId, "ACTIF");

        if (!activeClasses.isEmpty()) {
            // Récupérer le nom de la classe active
            String classeActiveName = activeClasses.get(0).getClasse().getNom();
            throw new RuntimeException("L'étudiant est déjà inscrit dans la classe : " + classeActiveName);
        }

        // Vérifier que la classe existe
        AcademicClass classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        // Vérifier que l'étudiant existe
        User etudiant = userRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        // Vérifier que c'est bien un étudiant
        if (!etudiant.hasRole("ROLE_STUDENT")) {
            throw new RuntimeException("L'utilisateur n'est pas un étudiant");
        }

        // Créer l'inscription
        InscriptionsClass inscription = new InscriptionsClass();
        inscription.setClasse(classe);
        inscription.setEtudiant(etudiant);
        inscription.setDateInscription(Instant.now());
        inscription.setStatut("ACTIF");

        return inscriptionsClassRepository.save(inscription);
    }

    /**
     * Retire un étudiant d'une classe (désactivation)
     */
    @Transactional
    public void unenrollStudent(Long classeId, Long etudiantId) {
        InscriptionsClass inscription = inscriptionsClassRepository
                .findByClasseIdAndEtudiantId(classeId, etudiantId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        // Option 1: Désactiver (garder l'historique)
        inscription.setStatut("INACTIF");
        inscriptionsClassRepository.save(inscription);

        // Option 2: Supprimer complètement
        // inscriptionsClassRepository.delete(inscription);
    }

    /**
     * Réactive un étudiant dans une classe
     */
    @Transactional
    public void reactivateStudent(Long classeId, Long etudiantId) {
        InscriptionsClass inscription = inscriptionsClassRepository
                .findByClasseIdAndEtudiantId(classeId, etudiantId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        inscription.setStatut("ACTIF");
        inscriptionsClassRepository.save(inscription);
    }

    /**
     * Supprime définitivement une inscription
     */
    @Transactional
    public void deleteEnrollment(Long classeId, Long etudiantId) {
        inscriptionsClassRepository.deleteByClasseIdAndEtudiantId(classeId, etudiantId);
    }

    /**
     * Inscrit plusieurs étudiants en masse
     */
    @Transactional
    public void enrollMultipleStudents(Long classeId, List<Long> etudiantIds) {
        AcademicClass classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        int successCount = 0;
        int skipCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Long etudiantId : etudiantIds) {
            try {
                // Vérifier si déjà inscrit dans cette classe
                if (inscriptionsClassRepository.existsByClasseIdAndEtudiantId(classeId, etudiantId)) {
                    skipCount++;
                    continue;
                }

                // ✅ Vérifier si l'étudiant a déjà une classe active
                List<InscriptionsClass> activeClasses = inscriptionsClassRepository
                        .findByEtudiantIdAndStatut(etudiantId, "ACTIF");

                if (!activeClasses.isEmpty()) {
                    skipCount++;
                    continue; // Passer au suivant
                }

                User etudiant = userRepository.findById(etudiantId)
                        .orElseThrow(() -> new RuntimeException("Étudiant " + etudiantId + " non trouvé"));

                if (etudiant.hasRole("ROLE_STUDENT")) {
                    InscriptionsClass inscription = new InscriptionsClass();
                    inscription.setClasse(classe);
                    inscription.setEtudiant(etudiant);
                    inscription.setDateInscription(Instant.now());
                    inscription.setStatut("ACTIF");
                    inscriptionsClassRepository.save(inscription);
                    successCount++;
                }
            } catch (Exception e) {
                skipCount++;
            }
        }

        if (skipCount > 0 && successCount == 0) {
            throw new RuntimeException("Aucun étudiant n'a pu être inscrit (" + skipCount + " déjà inscrits ou ayant une classe active)");
        }
    }
}