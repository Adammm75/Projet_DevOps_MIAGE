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
            // Désactivation CSRF pour les endpoints API
            .csrf(csrf -> csrf.disable())

            // Définition des règles d'accès
            .authorizeHttpRequests(auth -> auth
                // ✅ RÈGLE 1 : APIs publiques (tes endpoints de test)
                .requestMatchers("/api/inactivity/**").permitAll()
                .requestMatchers("/api/activity/**").permitAll()
                .requestMatchers("/api/v1/audio/upload").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/api/progress/**").permitAll()

                .requestMatchers("/api/student/**").permitAll()
                //.requestMatchers("/api/student/steps?**").permitAll()
                
                // ✅ RÈGLE 2 : Pages publiques (login, register, assets)
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                
                // ✅ RÈGLE 3 : Accès par rôle (pages web protégées)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                
                // ✅ RÈGLE 4 : Tout le reste nécessite authentification
                .anyRequest().authenticated()
            )

            // Configuration du login web classique
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // Configuration du logout
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