package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ✅ Trouve des utilisateurs par nom de rôle
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r " +
            "WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * ✅ Trouve un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * ✅ Vérifie si un email existe
     */
    boolean existsByEmail(String email);
}
