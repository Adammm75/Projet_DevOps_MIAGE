package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer le ciblage des cours vers les filières et classes
 * ADAPTÉ POUR LES CLÉS COMPOSITES
 */
@Service
@RequiredArgsConstructor
public class CourseTargetingService {

    private final CoursFiliereRepository coursFiliereRepository;
    private final CoursClassRepository coursClassRepository;
    private final FiliereRepository filiereRepository;
    private final AcademicClassRepository classeRepository;
    private final CoursRepository coursRepository;

    /**
     * Récupère toutes les filières disponibles
     */
    public List<Filiere> getAllFilieres() {
        return filiereRepository.findAll();
    }

    /**
     * Récupère toutes les classes disponibles
     */
    public List<AcademicClass> getAllClasses() {
        return classeRepository.findAll();
    }

    /**
     * Récupère les IDs des filières ciblées par un cours
     */
    public List<Long> getTargetedFiliereIds(Long courseId) {
        return coursFiliereRepository.findAll().stream()
                .filter(cf -> cf.getCours().getId().equals(courseId))
                .map(cf -> cf.getFiliere().getId())
                .collect(Collectors.toList());
    }

    /**
     * Récupère les IDs des classes ciblées par un cours
     */
    public List<Long> getTargetedClasseIds(Long courseId) {
        return coursClassRepository.findAll().stream()
                .filter(cc -> cc.getCours().getId().equals(courseId))
                .map(cc -> cc.getClasse().getId())
                .collect(Collectors.toList());
    }

    /**
     * Met à jour le ciblage complet d'un cours (filières ET classes)
     * Supprime les anciens ciblages et crée les nouveaux
     */
    @Transactional
    public void updateCourseTargeting(Long courseId, List<Long> filiereIds, List<Long> classeIds) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'ID : " + courseId));

        // ✅ ÉTAPE 1 : Supprimer tous les anciens ciblages
        List<CoursFiliere> existingFilieres = coursFiliereRepository.findAll().stream()
                .filter(cf -> cf.getCours().getId().equals(courseId))
                .collect(Collectors.toList());
        coursFiliereRepository.deleteAll(existingFilieres);

        List<CoursClass> existingClasses = coursClassRepository.findAll().stream()
                .filter(cc -> cc.getCours().getId().equals(courseId))
                .collect(Collectors.toList());
        coursClassRepository.deleteAll(existingClasses);

        // ✅ ÉTAPE 2 : Ajouter les nouvelles filières ciblées
        if (filiereIds != null && !filiereIds.isEmpty()) {
            for (Long filiereId : filiereIds) {
                Filiere filiere = filiereRepository.findById(filiereId)
                        .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'ID : " + filiereId));

                // Créer la clé composite
                CoursFiliereId id = new CoursFiliereId();
                id.setCoursId(courseId);
                id.setFiliereId(filiereId);

                CoursFiliere cf = new CoursFiliere();
                cf.setId(id);
                cf.setCours(cours);
                cf.setFiliere(filiere);
                coursFiliereRepository.save(cf);
            }
        }

        // ✅ ÉTAPE 3 : Ajouter les nouvelles classes ciblées
        if (classeIds != null && !classeIds.isEmpty()) {
            for (Long classeId : classeIds) {
                AcademicClass classe = classeRepository.findById(classeId)
                        .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID : " + classeId));

                // Créer la clé composite
                CoursClassId id = new CoursClassId();
                id.setCoursId(courseId);
                id.setClasseId(classeId);

                CoursClass cc = new CoursClass();
                cc.setId(id);
                cc.setCours(cours);
                cc.setClasse(classe);
                coursClassRepository.save(cc);
            }
        }
    }

    /**
     * Compte le nombre de filières ciblées par un cours
     */
    public long countTargetedFilieres(Long courseId) {
        return getTargetedFiliereIds(courseId).size();
    }

    /**
     * Compte le nombre de classes ciblées par un cours
     */
    public long countTargetedClasses(Long courseId) {
        return getTargetedClasseIds(courseId).size();
    }

    /**
     * Vérifie si un cours est ciblé vers une filière spécifique
     */
    public boolean isCourseTargetedToFiliere(Long courseId, Long filiereId) {
        return coursFiliereRepository.existsByCoursIdAndFiliereId(courseId, filiereId);
    }

    /**
     * Vérifie si un cours est ciblé vers une classe spécifique
     */
    public boolean isCourseTargetedToClasse(Long courseId, Long classeId) {
        return coursClassRepository.existsByCoursIdAndClasseId(courseId, classeId);
    }
}