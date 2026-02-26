package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.DashboardStatsDTO;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final UserRepository userRepository;
    private final CoursRepository coursRepository;

    /**
     * Calcule toutes les statistiques pour le dashboard admin
     */
    public DashboardStatsDTO calculateDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Récupérer tous les utilisateurs et cours
        List<User> allUsers = userRepository.findAll();
        List<Cours> allCourses = coursRepository.findAll();

        // Compteurs généraux
        stats.setTotalUsers(allUsers.size());
        stats.setTotalCourses(allCourses.size());

        // Compteurs par rôle
        long admins = allUsers.stream().filter(u -> u.hasRole("ROLE_ADMIN")).count();
        long teachers = allUsers.stream().filter(u -> u.hasRole("ROLE_TEACHER")).count();
        long students = allUsers.stream().filter(u -> u.hasRole("ROLE_STUDENT")).count();

        stats.setTotalAdmins(admins);
        stats.setTotalTeachers(teachers);
        stats.setTotalStudents(students);

        // Nouveaux utilisateurs aujourd'hui
        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long newUsersToday = allUsers.stream()
                .filter(u -> u.getCreatedAt().isAfter(todayStart))
                .count();
        stats.setNewUsersToday(newUsersToday);

        // Nouveaux cours aujourd'hui
        long newCoursesToday = allCourses.stream()
                .filter(c -> c.getCreatedAt().isAfter(todayStart))
                .count();
        stats.setNewCoursesToday(newCoursesToday);

        // Alertes système (pour l'instant fictif, à adapter selon vos besoins)
        stats.setSystemAlerts(0);

        // Graphique 1 : Inscriptions des 7 derniers jours
        stats.setLast7DaysRegistrations(calculateLast7DaysRegistrations(allUsers));

        // Graphique 2 : Répartition par rôle
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("Administrateurs", admins);
        usersByRole.put("Enseignants", teachers);
        usersByRole.put("Étudiants", students);
        stats.setUsersByRole(usersByRole);

        // Graphique 3 : Top 5 cours (fictif pour l'instant, à adapter)
        stats.setTopCourses(calculateTopCourses(allCourses));

        return stats;
    }

    /**
     * Calcule les inscriptions des 7 derniers jours
     */
    private List<DashboardStatsDTO.RegistrationDataPoint> calculateLast7DaysRegistrations(List<User> allUsers) {
        List<DashboardStatsDTO.RegistrationDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            long count = allUsers.stream()
                    .filter(u -> u.getCreatedAt().isAfter(dayStart) && u.getCreatedAt().isBefore(dayEnd))
                    .count();

            dataPoints.add(new DashboardStatsDTO.RegistrationDataPoint(
                    date.format(formatter),
                    count
            ));
        }

        return dataPoints;
    }

    /**
     * Calcule le top 5 des cours (par ordre de création récente pour l'instant)
     * TODO: Adapter selon le nombre réel d'étudiants par cours
     */
    private List<DashboardStatsDTO.CourseActivityData> calculateTopCourses(List<Cours> allCourses) {
        return allCourses.stream()
                .sorted(Comparator.comparing(Cours::getCreatedAt).reversed())
                .limit(5)
                .map(c -> new DashboardStatsDTO.CourseActivityData(
                        c.getTitle(),
                        c.getCode(),
                        0 // TODO: Remplacer par le vrai nombre d'étudiants
                ))
                .collect(Collectors.toList());
    }
}