package com.fptu.forum.controller;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly like va bookmark (interaction).
 * Tat ca yeu cau dang nhap.
 */
@Controller
@RequiredArgsConstructor
public class InteractionController {

    private final LikeService likeService;
    private final SavedPostService savedPostService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    // ---- Like / Unlike bai viet ----

    @PostMapping("/likes/post/{postId}")
    public String toggleLikePost(@PathVariable Long postId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        Post post = postService.findById(postId);
        boolean liked = likeService.toggleLikePost(user, post);
        redirectAttributes.addFlashAttribute("successMsg",
                liked ? "Da like bai viet!" : "Da bo like.");
        return "redirect:/posts/" + postId;
    }

    // ---- Like / Unlike comment ----

    @PostMapping("/likes/comment/{commentId}")
    public String toggleLikeComment(@PathVariable Long commentId,
                                    @RequestParam Long postId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        var comment = commentService.findById(commentId);
        likeService.toggleLikeComment(user, comment);
        return "redirect:/posts/" + postId;
    }

    // ---- Bookmark / Unbookmark ----

    @PostMapping("/bookmarks/post/{postId}")
    public String toggleBookmark(@PathVariable Long postId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        Post post = postService.findById(postId);
        boolean saved = savedPostService.toggleSave(user, post);
        redirectAttributes.addFlashAttribute("successMsg",
                saved ? "Da luu bai viet!" : "Da bo luu.");
        return "redirect:/posts/" + postId;
    }

    // ---- Xem danh sach bookmark ----

    @GetMapping("/bookmarks")
    public String listBookmarks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("savedPosts", savedPostService.listSavedPosts(user.getId()));
        return "forum/bookmarks";
    }
}
