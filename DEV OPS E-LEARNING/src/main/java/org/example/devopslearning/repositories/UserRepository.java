package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.ResourceConsultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r " +
            "WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query(value = """
    SELECT COUNT(DISTINCT u.id) FROM users u
    INNER JOIN user_roles ur ON ur.user_id = u.id
    INNER JOIN roles r ON r.id = ur.role_id
    WHERE r.name = 'ROLE_STUDENT'
    AND (
        EXISTS (SELECT 1 FROM assignment_submissions s WHERE s.student_id = u.id)
        OR EXISTS (SELECT 1 FROM resource_consultations c WHERE c.student_id = u.id)
    )
    """, nativeQuery = true)
    long countActiveStudents();
}