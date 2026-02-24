package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.UserRole;
import org.example.devopslearning.entities.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUser(User user);
    void deleteByUserId(Long userId);
}
