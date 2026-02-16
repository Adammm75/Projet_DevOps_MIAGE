package org.example.devopslearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // Compteurs généraux
    private long totalUsers;
    private long totalCourses;
    private long totalAdmins;
    private long totalTeachers;
    private long totalStudents;

    // Alertes système
    private long systemAlerts;
    private long newUsersToday;
    private long newCoursesToday;

    // Données pour graphiques
    private List<RegistrationDataPoint> last7DaysRegistrations;
    private Map<String, Long> usersByRole;
    private List<CourseActivityData> topCourses;

    // Classes internes pour les données de graphiques
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationDataPoint {
        private String date;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseActivityData {
        private String courseName;
        private String courseCode;
        private long studentCount;
    }
}