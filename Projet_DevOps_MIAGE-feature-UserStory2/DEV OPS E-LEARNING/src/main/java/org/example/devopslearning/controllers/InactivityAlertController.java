package org.example.devopslearning.controllers;

import org.example.devopslearning.entities.InactivityAlert;
import org.example.devopslearning.services.InactivityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inactivity")
public class InactivityAlertController {

    private final InactivityAlertService service;

    public InactivityAlertController(InactivityAlertService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API fonctionne !");
    }

    // Teacher: get open alerts for courses they teach
    @GetMapping("/teacher/{teacherId}/alerts")
    public ResponseEntity<List<InactivityAlert>> getAlertsForTeacher(@PathVariable Long teacherId) {
        List<InactivityAlert> alerts = service.findOpenAlertsForTeacher(teacherId);
        return ResponseEntity.ok(alerts);
    }

    // Student: get all alerts for them
    @GetMapping("/student/{studentId}/alerts")
    public ResponseEntity<List<InactivityAlert>> getAlertsForStudent(@PathVariable Long studentId) {
        List<InactivityAlert> alerts = service.findAlertsForStudent(studentId);
        return ResponseEntity.ok(alerts);
    }

    // Mark alert as handled by teacher (handlerId)
    @PostMapping("/alerts/{alertId}/handle")
    public ResponseEntity<Void> handleAlert(@PathVariable Long alertId, @RequestParam Long handlerId) {
        service.markAlertAsHandled(alertId, handlerId);
        return ResponseEntity.ok().build();
    }

    // Manual endpoint to trigger check (for dev)
    @PostMapping("/run-check")
    public ResponseEntity<Void> runCheckNow() {
        service.runInactivityCheckDefault();
        return ResponseEntity.ok().build();
    }


}
