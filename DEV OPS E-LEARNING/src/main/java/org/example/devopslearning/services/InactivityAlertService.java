package org.example.devopslearning.services;

//import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.example.devopslearning.services.NotificationService;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.InactivityAlert;
import org.example.devopslearning.repositories.InactivityAlertRepository;
import org.example.devopslearning.repositories.StudentActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class InactivityAlertService {


    private final UserRepository userRepository;
    private final CoursRepository courseRepository;
    private final StudentActivityRepository activityRepository;
    private final InactivityAlertRepository alertRepository;
    private final NotificationService notificationService;


    // default threshold (days)
    private final int DEFAULT_DAYS = 7;


    public InactivityAlertService(UserRepository userRepository,
    CoursRepository courseRepository,
    StudentActivityRepository activityRepository,
    InactivityAlertRepository alertRepository,
    NotificationService notificationService) 
    {
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
    this.activityRepository = activityRepository;
    this.alertRepository = alertRepository;
    this.notificationService = notificationService;
    }

    /**
    * Core method used by scheduler: checks all students and creates alerts when needed.
    */

    public void runInactivityCheck(int daysThreshold) {
        List<User> students = userRepository.findByRoleName("ROLE_STUDENT");


        LocalDateTime now = LocalDateTime.now();


        for (User student : students) {
            Long studentId = student.getId();


            // get enrolled courses for this student
            List<Cours> courses = courseRepository.findCoursesForStudent(studentId);


            for (Cours course : courses) {
                Long courseId = course.getId();
                // get last activity for this student in this course
                List<org.example.devopslearning.activity.StudentActivity> acts = activityRepository.findByStudentAndCourseOrderByActivityTimeDesc(studentId, courseId);


                Instant lastActivity = acts.isEmpty() ? null : acts.get(0).getActivityTime();


                long daysInactive = (lastActivity == null) ? Long.MAX_VALUE : Duration.between(lastActivity, now).toDays();


                // check if there's already an open alert
                List<InactivityAlert> existing = alertRepository.findByStudent_IdAndCourse_IdAndStatus(studentId, courseId, "OPEN");
                if (lastActivity == null || daysInactive >= daysThreshold) {
                    if (existing.isEmpty()) {
                        // create alert
                        InactivityAlert alert = alertRepository.save(InactivityAlert.builder()
                                    .student(student)
                                    .course(course)
                                    .daysInactive((lastActivity == null) ? (int) daysThreshold : (int) daysInactive)
                                    .lastActivityAt(lastActivity)
                                    .status("OPEN")
                                    .build());
                        
                        // create internal notification for teacher(s)
                        List<User> teachers = courseRepository.findTeachersForCourse(courseId);
                        String title = "Alerte d'inactivité: " + student.getFirstName() + " " + student.getLastName();
                        String content = "L'étudiant(e) " + student.getFirstName() + " n'a pas été actif(ve) depuis " + ((lastActivity==null)?"plusieurs jours":daysInactive + " jours") + " sur le cours " + course.getTitle();


                        // notify teachers
                        teachers.forEach(t -> notificationService.createNotification(t.getId(), "INACTIVITY_ALERT", title, content, courseId, alert.getId()));


                        // notify student
                        notificationService.createNotification(studentId, "INACTIVITY_ALERT", "Tu es inactif(ve)", content, courseId, alert.getId());
                    }
                } else {
                    // student is active: close any open alerts
                    if (!existing.isEmpty()) {
                        for (InactivityAlert a : existing) {
                            a.setStatus("CLOSED");
                            a.setClosedAt(Instant.now());
                            a.setDaysInactive((int)daysInactive);
                            alertRepository.save(a);


                            // notify teacher & student about closure
                            notificationService.createNotification(a.getStudent().getId(), "INACTIVITY_RESOLVED", "Alerte résolue", "Ton activité a repris.", courseId, a.getId());
                            List<User> teachers = courseRepository.findTeachersForCourse(courseId);
                            teachers.forEach(t -> notificationService.createNotification(t.getId(), "INACTIVITY_RESOLVED", "Alerte résolue pour " + a.getStudent().getFirstName(), "L'étudiant a repris son activité.", courseId, a.getId()));
                        }
                    }
                }
            }
        }
    }

    public void runInactivityCheckDefault() {
        runInactivityCheck(DEFAULT_DAYS);
    }

    public List<InactivityAlert> findOpenAlertsForTeacher(Long teacherId) {
        // fetch courses taught by teacher
        List<Cours> taught = courseRepository.findCoursesByTeacherId(teacherId);
        List<Long> courseIds = taught.stream().map(Cours::getId).collect(Collectors.toList());
        return alertRepository.findAll().stream().filter(a -> courseIds.contains(a.getCourse().getId()) && "OPEN".equals(a.getStatus())).collect(Collectors.toList());
    }

    public List<InactivityAlert> findAlertsForStudent(Long studentId) {
        return alertRepository.findAll().stream().filter(a -> a.getStudent().getId().equals(studentId)).collect(Collectors.toList());
    }

    public void markAlertAsHandled(Long alertId, Long handlerUserId) {
        InactivityAlert a = alertRepository.findById(alertId).orElseThrow(() -> new RuntimeException("Alert not found"));
        a.setStatus("CLOSED");
        a.setClosedAt(Instant.now());
        // set handledBy minimal (load user reference)
        User handler = userRepository.findById(handlerUserId).orElse(null);
        a.setHandledBy(handler);
        alertRepository.save(a);
    }
}


