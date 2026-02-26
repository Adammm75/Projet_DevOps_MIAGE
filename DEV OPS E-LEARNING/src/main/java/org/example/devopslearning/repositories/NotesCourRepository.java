package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotesCourRepository extends JpaRepository<NotesCour, Long> {

    // ========================================
    // RÉCUPÉRATION PAR COURS ET ÉTUDIANT
    // ========================================

    /**
     * Trouve une note par cours et étudiant (objets complets)
     */
    Optional<NotesCour> findByCoursAndEtudiant(Cours cours, User etudiant);

    /**
     * Trouve une note par IDs
     */
    Optional<NotesCour> findByCoursIdAndEtudiantId(Long coursId, Long etudiantId);

    // ========================================
    // RÉCUPÉRATION PAR COURS
    // ========================================

    /**
     * Trouve toutes les notes d'un cours
     */
    List<NotesCour> findByCoursId(Long coursId);

    /**
     * Trouve toutes les notes d'un cours (par objet Cours)
     */
    List<NotesCour> findByCours(Cours cours);

    /**
     * Trouve les notes d'un cours triées par date
     */
    List<NotesCour> findByCoursIdOrderByCreatedAtDesc(Long coursId);

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT
    // ========================================

    /**
     * Trouve toutes les notes d'un étudiant
     */
    List<NotesCour> findByEtudiantId(Long etudiantId);

    /**
     * Trouve les notes d'un étudiant triées par date
     */
    List<NotesCour> findByEtudiantIdOrderByCreatedAtDesc(Long etudiantId);

    // ========================================
    // RÉCUPÉRATION PAR ÉTUDIANT ET COURS
    // ========================================

    /**
     * Trouve toutes les notes d'un étudiant pour un cours
     */
    List<NotesCour> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    /**
     * Trouve les notes d'un étudiant pour un cours triées par date
     */
    List<NotesCour> findByEtudiantIdAndCoursIdOrderByCreatedAtDesc(Long etudiantId, Long coursId);

    // ❌ SUPPRIMÉE - Cette méthode causait l'erreur car NotesCour n'a pas de champ 'type'
    // Optional<NotesCour> findByEtudiantIdAndCoursIdAndType(Long etudiantId, Long coursId, String type);

    // ========================================
    // RÉCUPÉRATION PAR STATUT
    // ========================================

    /**
     * Trouve les notes par statut
     */
    List<NotesCour> findByStatut(String statut);

    /**
     * Trouve les notes d'un cours par statut
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.statut = :statut")
    List<NotesCour> findByCoursIdAndStatut(@Param("coursId") Long coursId, @Param("statut") String statut);

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte le nombre de notes d'un cours
     */
    long countByCoursId(Long coursId);

    /**
     * Compte le nombre de notes d'un étudiant
     */
    long countByEtudiantId(Long etudiantId);

    /**
     * Compte les notes d'un étudiant pour un cours
     */
    long countByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Calcule la moyenne générale d'un étudiant
     */
    @Query("SELECT AVG(n.noteFinale) FROM NotesCour n WHERE n.etudiant.id = :etudiantId")
    BigDecimal calculateAverageByStudent(@Param("etudiantId") Long etudiantId);

    /**
     * Calcule la moyenne d'un étudiant pour un cours
     */
    @Query("SELECT AVG(n.noteFinale) FROM NotesCour n WHERE n.etudiant.id = :etudiantId AND n.cours.id = :coursId")
    BigDecimal calculateAverageByCourse(@Param("etudiantId") Long etudiantId, @Param("coursId") Long coursId);

    /**
     * Note maximale d'un étudiant
     */
    @Query("SELECT MAX(n.noteFinale) FROM NotesCour n WHERE n.etudiant.id = :etudiantId")
    BigDecimal findHighestGradeByStudent(@Param("etudiantId") Long etudiantId);

    /**
     * Note minimale d'un étudiant
     */
    @Query("SELECT MIN(n.noteFinale) FROM NotesCour n WHERE n.etudiant.id = :etudiantId")
    BigDecimal findLowestGradeByStudent(@Param("etudiantId") Long etudiantId);

    /**
     * Moyenne d'un cours
     */
    @Query("SELECT AVG(n.noteFinale) FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale IS NOT NULL")
    Double getAverageGradeByCourseId(@Param("coursId") Long coursId);

    // ========================================
    // REQUÊTES POUR ENSEIGNANTS
    // ========================================

    /**
     * Trouve les notes des cours créés par un enseignant
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.createdBy.id = :teacherId")
    List<NotesCour> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Top notes d'un cours
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale IS NOT NULL ORDER BY n.noteFinale DESC")
    List<NotesCour> findTopGradesByCourseId(@Param("coursId") Long coursId);

    /**
     * Étudiants en difficulté (note < 10)
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale < 10")
    List<NotesCour> findStudentsAtRiskByCourseId(@Param("coursId") Long coursId);

    /**
     * Compte les étudiants ayant réussi un cours (note >= 10)
     */
    @Query("SELECT COUNT(n) FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale >= 10")
    long countPassingStudentsByCourseId(@Param("coursId") Long coursId);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    /**
     * Vérifie si un étudiant a une note pour un cours
     */
    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
}