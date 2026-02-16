package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotesCourRepository extends JpaRepository<NotesCour, Long> {

    /**
     * ✅ MÉTHODE UTILISÉE PAR GradeService
     * Trouve une note par cours et étudiant (objets complets)
     */
    Optional<NotesCour> findByCoursAndEtudiant(Cours cours, User etudiant);

    /**
     * Trouve toutes les notes d'un étudiant
     */
    List<NotesCour> findByEtudiantId(Long etudiantId);

    /**
     * Trouve une note par IDs (alternative)
     */
    Optional<NotesCour> findByCoursIdAndEtudiantId(Long coursId, Long etudiantId);

    /**
     * Trouve toutes les notes d'un cours
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId")
    List<NotesCour> findByCoursId(@Param("coursId") Long coursId);

    /**
     * Compte le nombre de notes d'un cours
     */
    long countByCoursId(Long coursId);

    /**
     * Trouve les notes par statut
     */
    List<NotesCour> findByStatut(String statut);

    /**
     * Trouve les notes d'un cours par statut
     */
    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.statut = :statut")
    List<NotesCour> findByCoursIdAndStatut(@Param("coursId") Long coursId, @Param("statut") String statut);

    /**
     * ✅ NOUVEAU : Trouve toutes les notes d'un cours (par objet Cours)
     */

    @Query("SELECT n FROM NotesCour n WHERE n.cours.createdBy.id = :teacherId")
    List<NotesCour> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale IS NOT NULL ORDER BY n.noteFinale DESC")
    List<NotesCour> findTopGradesByCourseId(@Param("coursId") Long coursId);

    @Query("SELECT n FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale < 10")
    List<NotesCour> findStudentsAtRiskByCourseId(@Param("coursId") Long coursId);

    @Query("SELECT COUNT(n) FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale >= 10")
    long countPassingStudentsByCourseId(@Param("coursId") Long coursId);

    @Query("SELECT AVG(n.noteFinale) FROM NotesCour n WHERE n.cours.id = :coursId AND n.noteFinale IS NOT NULL")
    Double getAverageGradeByCourseId(@Param("coursId") Long coursId);

    List<NotesCour> findByCours(Cours cours);

}