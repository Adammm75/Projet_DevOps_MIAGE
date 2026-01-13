package com.elearning.repositories;

// import com.elearning.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.elearning.entities.User;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // List<User> findAllByRoles_Name(String roleName);
    
    // IMPORTANT: adapt to ta structure userRoles -> role -> name
    // compare the role by its name string (ur.role.name) — ur.role is an entity so comparing ur.role = :roleName causes a type mismatch
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.role.name = :roleName")
    List<User> findAllByUserRoles_Name(@Param("roleName") String roleName);

}
