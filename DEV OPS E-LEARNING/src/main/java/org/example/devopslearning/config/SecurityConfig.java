package org.example.devopslearning.config;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ RÈGLE 1 : APIs publiques (tes endpoints de test)
                        .requestMatchers("/api/inactivity/**").permitAll()
                        .requestMatchers("/api/activity/**").permitAll()
                        .requestMatchers("/api/v1/audio/upload").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/progress/**").permitAll()

                        .requestMatchers("/api/student/**").permitAll()
                        // Pages publiques
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/v1/audio/upload").permitAll()

                        // Accès par rôle
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/student/**").hasRole("STUDENT")

                        // ✅ AJOUT : progression accessible aux étudiants et enseignants
                        .requestMatchers("/progression/student/**").hasRole("STUDENT")
                        .requestMatchers("/progression/teacher/**").hasRole("TEACHER")

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
