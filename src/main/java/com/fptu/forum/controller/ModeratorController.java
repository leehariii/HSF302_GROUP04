package com.fptu.forum.controller;

import com.fptu.forum.dto.request.MuteRequest;
import com.fptu.forum.entity.Report;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller danh cho Moderator: kiem duyet report, an/xoa bai, pin bai, mute user.
 * URL: /moderator/** - chi MODERATOR tro len moi truy cap.
 */
@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final ReportService reportService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final RestrictionService restrictionService;
    private final AuditLogService auditLogService;

    // Helper lay current user
    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    // ---- Danh sach report PENDING ----

    @GetMapping("/reports")
    public String pendingReports(Model model,
                                 @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("reports",
                reportService.findPendingReports(PageRequest.of(page, 20)));
        model.addAttribute("pendingCount", reportService.countPending());
        return "moderator/reports";
    }

    // ---- Chi tiet report ----

    @GetMapping("/reports/{id}")
    public String reportDetail(@PathVariable Long id, Model model) {
        Report report = reportService.findById(id);
        model.addAttribute("report", report);
        return "moderator/report-detail";
    }

    // ---- Resolve report (vi pham) ----

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id,
                                @RequestParam(required = false) String postAction,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User mod = getCurrentUser(userDetails);
            reportService.resolveReport(id, mod, postAction);
            redirectAttributes.addFlashAttribute("successMsg", "Da xu ly report.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/moderator/reports";
    }

    // ---- Reject report (khong vi pham) ----

    @PostMapping("/reports/{id}/reject")
    public String rejectReport(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User mod = getCurrentUser(userDetails);
            reportService.rejectReport(id, mod);
            redirectAttributes.addFlashAttribute("successMsg", "Da tu choi report.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/moderator/reports";
    }

    // ---- Pin / Unpin bai viet ----

    @PostMapping("/posts/{postId}/pin")
    public String pinPost(@PathVariable Long postId,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        postService.pinPost(postId);
        auditLogService.logPostAction(mod, AuditAction.PIN_POST,
                postService.findById(postId), null);
        redirectAttributes.addFlashAttribute("successMsg", "Da pin bai viet.");
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/unpin")
    public String unpinPost(@PathVariable Long postId,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        postService.unpinPost(postId);
        auditLogService.logPostAction(mod, AuditAction.UNPIN_POST,
                postService.findById(postId), null);
        redirectAttributes.addFlashAttribute("successMsg", "Da bo pin bai viet.");
        return "redirect:/posts/" + postId;
    }

    // ---- An bai viet vi pham ----

    @PostMapping("/posts/{postId}/hide")
    public String hidePost(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        postService.hidePost(postId);
        auditLogService.logPostAction(mod, AuditAction.HIDE_POST,
                postService.findById(postId), "An bai vi pham");
        redirectAttributes.addFlashAttribute("successMsg", "Da an bai viet.");
        return "redirect:/moderator/reports";
    }

    // ---- Xoa mem bai viet vi pham ----

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        postService.deletePost(postId);
        auditLogService.logPostAction(mod, AuditAction.DELETE_POST,
                postService.findById(postId), "Xoa bai vi pham");
        redirectAttributes.addFlashAttribute("successMsg", "Da xoa bai viet.");
        return "redirect:/moderator/reports";
    }

    // ---- Xoa mem comment vi pham ----

    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long postId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        var comment = commentService.findById(commentId);
        commentService.softDeleteComment(commentId);
        auditLogService.logCommentAction(mod, AuditAction.DELETE_COMMENT, comment,
                "Xoa comment vi pham");
        redirectAttributes.addFlashAttribute("successMsg", "Da xoa binh luan.");
        return "redirect:/posts/" + postId;
    }

    // ---- Mute user ----

    @GetMapping("/users/{userId}/mute")
    public String mutePage(@PathVariable Long userId, Model model) {
        model.addAttribute("targetUser", userService.findById(userId));
        model.addAttribute("muteRequest", new MuteRequest());
        return "moderator/mute-form";
    }

    @PostMapping("/users/{userId}/mute")
    public String muteUser(@PathVariable Long userId,
                           @Valid @ModelAttribute("muteRequest") MuteRequest request,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            User mod = getCurrentUser(userDetails);
            User target = userService.findById(userId);

            switch (request.getRestrictionType()) {
                case MUTE_POST -> {
                    restrictionService.mutePost(target, mod, request.getReason(), request.getEndAt());
                    auditLogService.logUserAction(mod, AuditAction.MUTE_POST, target,
                            request.getReason());
                }
                case MUTE_COMMENT -> {
                    restrictionService.muteComment(target, mod, request.getReason(), request.getEndAt());
                    auditLogService.logUserAction(mod, AuditAction.MUTE_COMMENT, target,
                            request.getReason());
                }
            }
            redirectAttributes.addFlashAttribute("successMsg", "Da mute user thanh cong.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + userId;
    }

    // ---- Unmute user ----

    @PostMapping("/users/{userId}/unmute")
    public String unmuteUser(@PathVariable Long userId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User mod = getCurrentUser(userDetails);
        User target = userService.findById(userId);
        restrictionService.unmuteAll(userId);
        auditLogService.logUserAction(mod, AuditAction.UNMUTE_USER, target, null);
        redirectAttributes.addFlashAttribute("successMsg", "Da unmute user.");
        return "redirect:/admin/users/" + userId;
    }
}
