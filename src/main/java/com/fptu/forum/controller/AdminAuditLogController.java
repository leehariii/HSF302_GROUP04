package com.fptu.forum.controller;

import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller cho Admin xem Audit Log.
 * URL: /admin/audit-logs
 */
@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String auditLogs(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "30") int size,
                            @RequestParam(required = false) String actorUsername,
                            @RequestParam(required = false) String action) {

        var logs = auditLogService.searchLogs(
                actorUsername, action,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("logs",          logs);
        model.addAttribute("actorUsername", actorUsername);
        model.addAttribute("action",        action);
        model.addAttribute("auditActions",  AuditAction.values());
        return "admin/audit-logs";
    }
}
