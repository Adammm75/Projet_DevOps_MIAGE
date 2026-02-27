package org.example.devopslearning.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new HashSet<>();

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // ⭐ AJOUTÉ - Numéro de téléphone
    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // =========================================================
    // ✅ Implémentation UserDetails
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // =========================================================
    // 🔥 Méthodes helper
    // =========================================================

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(String roleName) {
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().getName().equals(roleName));
    }

    /**
     * Récupère le rôle principal (le premier)
     */
    public Role getPrimaryRole() {
        return userRoles.stream()
                .findFirst()
                .map(UserRole::getRole)
                .orElse(null);
    }
}