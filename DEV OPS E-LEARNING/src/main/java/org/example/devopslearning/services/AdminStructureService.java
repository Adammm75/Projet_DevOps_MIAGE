package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStructureService {

    private final FiliereRepository filiereRepo;
    private final ParcourRepository parcourRepo;
    private final AcademicClassRepository classRepo;
    private final InscriptionsClassRepository inscriptionsRepo;
    private final NiveauxEtudeRepository niveauxEtudeRepository;

    // ==================== FILIÈRES ====================

    public Filiere createFiliere(Filiere f) {
        return filiereRepo.save(f);
    }

    public List<Filiere> getAllFilieres() {
        return filiereRepo.findAll();
    }

    // ==================== PARCOURS ====================

    public Parcour createParcours(String nom, String description, Long filiereId, Long niveauId) {
        // Récupérer la filière
        Filiere filiere = filiereRepo.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée avec l'ID : " + filiereId));

        // Récupérer le niveau
        NiveauxEtude niveau = niveauxEtudeRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé avec l'ID : " + niveauId));

        // Créer le parcours
        Parcour parcour = new Parcour();
        parcour.setNom(nom);
        parcour.setDescription(description);
        parcour.setFiliere(filiere);
        parcour.setNiveau(niveau);

        return parcourRepo.save(parcour);
    }

    public List<Parcour> getAllParcours() {
        return parcourRepo.findAll();
    }

    // ==================== CLASSES ====================

    public AcademicClass createClasse(String nom, String code, Long parcoursId, String anneeUniversitaire) {
        // Récupérer le parcours
        Parcour parcours = parcourRepo.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé avec l'ID : " + parcoursId));

        // Créer la classe
        AcademicClass classe = new AcademicClass();
        classe.setNom(nom);
        classe.setCode(code);
        classe.setParcours(parcours);
        classe.setAnneeUniversitaire(anneeUniversitaire);

        return classRepo.save(classe);
    }

    public List<AcademicClass> getAllClasses() {
        return classRepo.findAll();
    }

    // ==================== INSCRIPTIONS ====================

    public void inscrireEtudiant(Long classeId, User etudiant) {
        AcademicClass classe = classRepo.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID : " + classeId));

        InscriptionsClass ic = new InscriptionsClass();
        ic.setClasse(classe);
        ic.setEtudiant(etudiant);

        inscriptionsRepo.save(ic);
    }
}