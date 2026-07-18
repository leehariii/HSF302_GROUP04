package com.fptu.forum.controller;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller xu ly like va bookmark (interaction).
 * Tat ca yeu cau dang nhap.
 * Like/Unlike tra ve JSON de frontend cap nhat UI khong can reload.
 */
@Controller
@RequiredArgsConstructor
public class InteractionController {

    private final LikeService likeService;
    private final SavedPostService savedPostService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    // ---- Like / Unlike bai viet (AJAX -> JSON) ----

    @PostMapping("/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Post post = postService.findById(postId);
        boolean liked = likeService.toggleLikePost(user, post);
        long likeCount = likeService.countPostLikes(postId);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }

    // ---- Like / Unlike comment (AJAX -> JSON) ----

    @PostMapping("/comments/{commentId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        var comment = commentService.findById(commentId);
        boolean liked = likeService.toggleLikeComment(user, comment);
        long likeCount = likeService.countCommentLikes(commentId);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }

    // ---- Save / Unsave bai viet (AJAX -> JSON) ----

    @PostMapping("/posts/{postId}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSave(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Post post = postService.findById(postId);
        boolean saved = savedPostService.toggleSave(user, post);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    // ---- Xem danh sach bookmark (co phan trang) ----

    @GetMapping("/member/saved-posts")
    public String listBookmarks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("savedPosts", savedPostService.listSavedPosts(user.getId(), pageable));
        model.addAttribute("currentPage", page);
        return "forum/bookmarks";
    }
}

