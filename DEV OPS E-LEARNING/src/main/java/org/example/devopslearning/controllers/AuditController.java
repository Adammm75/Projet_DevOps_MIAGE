package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.AuditLog;
import org.example.devopslearning.services.AuditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/audit")
public class AuditController {

    private final AuditService auditService;

    /**
     * Page d'historique des actions
     */
    @GetMapping
    public String auditPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String entityType,
            Model model) {

        List<AuditLog> logs;

        if (search != null && !search.trim().isEmpty()) {
            logs = auditService.searchLogs(search);
            model.addAttribute("search", search);
        } else if (entityType != null && !entityType.trim().isEmpty()) {
            logs = auditService.getLogsByEntityType(entityType);
            model.addAttribute("entityType", entityType);
        } else {
            logs = auditService.getAllLogs();
        }

        model.addAttribute("logs", logs);

        return "admin/audit/list";
    }

    /**
     * Historique pour une entité spécifique
     */
    @GetMapping("/{entityType}/{entityId}")
    public String entityAudit(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Model model) {

        List<AuditLog> logs = auditService.getLogsByEntity(entityType, entityId);

        model.addAttribute("logs", logs);
        model.addAttribute("entityType", entityType);
        model.addAttribute("entityId", entityId);

        return "admin/audit/entity-history";
    }
}