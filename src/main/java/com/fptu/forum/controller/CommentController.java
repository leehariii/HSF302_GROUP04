package com.fptu.forum.controller;

import com.fptu.forum.dto.request.CommentRequest;
import com.fptu.forum.entity.Comment;
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
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    /**
     * Dang binh luan (POST form).
     * Sau khi binh luan, redirect ve bai viet.
     */
    @PostMapping("/posts/{postId}/comments")
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

    /**
     * Dang reply (POST form).
     */
    @PostMapping("/comments/{commentId}/reply")
    public String replyComment(@PathVariable Long commentId,
                               @Valid @ModelAttribute CommentRequest request,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Long postId = null;
        try {
            Comment parent = commentService.findById(commentId);
            postId = parent.getPost().getId();
            User user = userService.findByUsername(userDetails.getUsername());
            
            request.setParentCommentId(commentId);
            commentService.createComment(postId, request, user);
            redirectAttributes.addFlashAttribute("successMsg", "Tra loi binh luan thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return postId != null ? "redirect:/posts/" + postId : "redirect:/";
    }

    /**
     * Xoa mem binh luan (Cua chinh user)
     */
    @PostMapping("/comments/{commentId}/delete")
    public String deleteMyComment(@PathVariable Long commentId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        Long postId = null;
        try {
            Comment comment = commentService.findById(commentId);
            postId = comment.getPost().getId();
            User user = userService.findByUsername(userDetails.getUsername());
            
            commentService.deleteMyComment(commentId, user);
            redirectAttributes.addFlashAttribute("successMsg", "Xoa binh luan thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return postId != null ? "redirect:/posts/" + postId : "redirect:/";
    }
}
