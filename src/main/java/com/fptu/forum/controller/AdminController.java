package com.fptu.forum.controller;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller danh cho Admin: quan ly topic, user, xem audit logs.
 * URL: /admin/** - chi ADMIN moi truy cap.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final TopicService topicService;
    private final AuditLogService auditLogService;
    private final ReportService reportService;
    private final RestrictionService restrictionService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    // ---- Dashboard ----

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("pendingReports", reportService.countPending());
        model.addAttribute("topics", topicService.findAll());
        return "admin/dashboard";
    }

    // ========== USER MANAGEMENT ==========

    @GetMapping("/users")
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("users", userService.searchUsers(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("users", userService.findAll(PageRequest.of(page, 20)));
        }
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User target = userService.findById(id);
        model.addAttribute("targetUser", target);
        model.addAttribute("restrictions", restrictionService.findByUserId(id));
        return "admin/user-detail";
    }

    @PostMapping("/users/{id}/ban")
    public String banUser(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(userDetails);
            User target = userService.findById(id);
            userService.banUser(id, admin);
            auditLogService.logUserAction(admin, AuditAction.BAN_USER, target, null);
            redirectAttributes.addFlashAttribute("successMsg", "Da ban user.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/unban")
    public String unbanUser(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User admin = getCurrentUser(userDetails);
        User target = userService.findById(id);
        userService.unbanUser(id);
        auditLogService.logUserAction(admin, AuditAction.UNBAN_USER, target, null);
        redirectAttributes.addFlashAttribute("successMsg", "Da unban user.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/promote")
    public String promote(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(userDetails);
            User target = userService.findById(id);
            userService.promoteToModerator(id);
            auditLogService.logUserAction(admin, AuditAction.PROMOTE_MODERATOR, target, null);
            redirectAttributes.addFlashAttribute("successMsg", "Da promote len MODERATOR.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/demote")
    public String demote(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(userDetails);
            User target = userService.findById(id);
            userService.demoteModerator(id);
            auditLogService.logUserAction(admin, AuditAction.DEMOTE_MODERATOR, target, null);
            redirectAttributes.addFlashAttribute("successMsg", "Da demote xuong MEMBER.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ========== TOPIC MANAGEMENT ==========

    @GetMapping("/topics")
    public String listTopics(Model model) {
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("topicRequest", new TopicRequest());
        return "admin/topics";
    }

    @GetMapping("/topics/create")
    public String createTopicForm(Model model) {
        model.addAttribute("topicRequest", new TopicRequest());
        model.addAttribute("isEdit", false);
        return "admin/topic-form";
    }

    @PostMapping("/topics/create")
    public String createTopic(@Valid @ModelAttribute("topicRequest") TopicRequest request,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/topic-form";
        }
        try {
            User admin = getCurrentUser(userDetails);
            var topic = topicService.create(request);
            auditLogService.log(admin, AuditAction.CREATE_TOPIC, null, null, null,
                    "Tao topic: " + topic.getName());
            redirectAttributes.addFlashAttribute("successMsg", "Tao topic thanh cong.");
            return "redirect:/admin/topics";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/topics/create";
        }
    }

    @GetMapping("/topics/edit/{id}")
    public String editTopicForm(@PathVariable Long id, Model model) {
        var topic = topicService.findById(id);
        TopicRequest request = new TopicRequest();
        request.setName(topic.getName());
        request.setDescription(topic.getDescription());
        model.addAttribute("topicRequest", request);
        model.addAttribute("topicId", id);
        model.addAttribute("isEdit", true);
        return "admin/topic-form";
    }

    @PostMapping("/topics/edit/{id}")
    public String updateTopic(@PathVariable Long id,
                              @Valid @ModelAttribute("topicRequest") TopicRequest request,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/topic-form";
        }
        try {
            User admin = getCurrentUser(userDetails);
            topicService.update(id, request);
            auditLogService.log(admin, AuditAction.UPDATE_TOPIC, null, null, null,
                    "Cap nhat topic id=" + id);
            redirectAttributes.addFlashAttribute("successMsg", "Cap nhat topic thanh cong.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/topics";
    }

    @PostMapping("/topics/delete/{id}")
    public String deleteTopic(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(userDetails);
            auditLogService.log(admin, AuditAction.DELETE_TOPIC, null, null, null,
                    "Xoa topic id=" + id);
            topicService.delete(id);
            redirectAttributes.addFlashAttribute("successMsg", "Da xoa topic.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/topics";
    }

    // ========== AUDIT LOGS ==========

    @GetMapping("/audit-logs")
    public String auditLogs(Model model,
                            @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("logs",
                auditLogService.getAllLogs(PageRequest.of(page, 30)));
        return "admin/audit-logs";
    }
}
