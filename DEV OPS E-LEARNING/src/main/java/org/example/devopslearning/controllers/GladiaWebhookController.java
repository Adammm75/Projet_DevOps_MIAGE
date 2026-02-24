package org.example.devopslearning.controllers;

import org.example.devopslearning.dto.GladiaWebhookDto;
import org.example.devopslearning.services.TranscriptionOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gladia")
public class GladiaWebhookController {

    private final TranscriptionOrchestrationService orchestrationService;

    public GladiaWebhookController(TranscriptionOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody GladiaWebhookDto payload) {
        orchestrationService.processGladiaPayload(payload);
        return ResponseEntity.ok(java.util.Map.of("status", "accepted"));
    }
}
