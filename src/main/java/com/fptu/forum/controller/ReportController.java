package com.fptu.forum.controller;

import com.fptu.forum.dto.request.ReportRequest;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.ReportService;
import com.fptu.forum.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly bao cao vi pham.
 * Member co the report post hoac comment.
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    // Report bai viet
    @PostMapping("/post/{postId}")
    public String reportPost(@PathVariable Long postId,
                             @Valid @ModelAttribute ReportRequest request,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            reportService.createReportPost(postId, request, user);
            redirectAttributes.addFlashAttribute("successMsg", "Da gui bao cao. Cam on ban!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    // Report comment
    @PostMapping("/comment/{commentId}")
    public String reportComment(@PathVariable Long commentId,
                                @RequestParam Long postId,
                                @Valid @ModelAttribute ReportRequest request,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            reportService.createReportComment(commentId, request, user);
            redirectAttributes.addFlashAttribute("successMsg", "Da gui bao cao. Cam on ban!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }
}
