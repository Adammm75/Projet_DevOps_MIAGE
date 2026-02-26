package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    List<User> findByRoleName(String roleName);

    // Étudiants actifs = ayant une activité dans les 30 derniers jours
    @Query("SELECT COUNT(DISTINCT sa.student.id) FROM StudentActivity sa WHERE sa.activityTime >= :since")
    long countActiveStudentsSince(@Param("since") Instant since);
}