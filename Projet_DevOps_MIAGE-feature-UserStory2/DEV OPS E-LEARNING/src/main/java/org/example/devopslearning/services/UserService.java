package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.RegisterRequest;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request, String roleName) {

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Création utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now());

        user = userRepository.save(user);

        // Récupération du rôle
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role introuvable : " + roleName));

        // Construction de l'ID composite
        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(user.getId());
        userRoleId.setRoleId(role.getId());

        // Création du lien User ↔ Role
        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Trouve un utilisateur par son username (email)
     */
    public User findByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + username));
    }

    /**
     * Trouve un utilisateur par son ID
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Liste tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ✅ NOUVEAU : Mettre à jour un utilisateur
     */
    @Transactional
    public User updateUser(User user) {
        User existingUser = findById(user.getId());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    /**
     * ✅ NOUVEAU : Supprimer un utilisateur
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);

        // Supprimer d'abord les rôles associés
        userRoleRepository.deleteByUserId(userId);

        // Puis supprimer l'utilisateur
        userRepository.delete(user);
    }

    public User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
    }
}