package com.fptu.forum.controller;

import com.fptu.forum.dto.request.CommentRequest;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.CommentService;
import com.fptu.forum.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly binh luan (comment, reply).
 * Yeu cau dang nhap.
 */
@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    /**
     * Dang binh luan / reply (POST form).
     * Sau khi binh luan, redirect ve bai viet.
     */
    @PostMapping("/post/{postId}")
    public String addComment(@PathVariable Long postId,
                             @Valid @ModelAttribute CommentRequest request,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            commentService.createComment(postId, request, user);
            redirectAttributes.addFlashAttribute("successMsg", "Binh luan thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }
}
