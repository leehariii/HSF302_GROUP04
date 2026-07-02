package com.fptu.forum.controller;

import com.fptu.forum.dto.request.PostRequest;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly bai viet: xem, tao, sua, xoa mem.
 * Cac trang xem public, cac trang tao/sua yeu cau dang nhap.
 */
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final TopicService topicService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final SavedPostService savedPostService;
    private final UserService userService;

    // ---- Xem chi tiet bai viet ----

    @GetMapping("/{id}")
    public String postDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userService.findByUsername(userDetails.getUsername());
        }

        Post post = postService.getPostForDetailView(id, currentUser);

        // Chi tang view count neu bai viet ACTIVE (public)
        if (post.getStatus() == com.fptu.forum.enums.PostStatus.ACTIVE) {
            postService.increaseViewCount(id);
        }

        model.addAttribute("post", post);
        model.addAttribute("rootComments", commentService.findRootComments(id));
        model.addAttribute("commentCount", commentService.countActiveComments(id));
        model.addAttribute("likeCount", likeService.countPostLikes(id));

        // Neu da dang nhap, lay trang thai like/bookmark cua user
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("currentUser", user);
            model.addAttribute("isLiked", likeService.isPostLiked(user.getId(), id));
            model.addAttribute("isSaved", savedPostService.isSaved(user.getId(), id));
        }

        return "forum/post-detail";
    }

    // ---- Form tao bai viet moi ----

    @GetMapping("/new")
    public String newPostForm(Model model) {
        model.addAttribute("postRequest", new PostRequest());
        model.addAttribute("topics", topicService.findAll());
        return "forum/post-form";
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute("postRequest") PostRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            return "forum/post-form";
        }

        try {
            User author = userService.findByUsername(userDetails.getUsername());
            Post post = postService.createPost(request, author);
            redirectAttributes.addFlashAttribute("successMsg", "Dang bai thanh cong!");
            return "redirect:/posts/" + post.getId();
        } catch (Exception e) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("errorMsg", e.getMessage());
            return "forum/post-form";
        }
    }

    // ---- Form sua bai viet ----

    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Post post = postService.findById(id);
        User user = userService.findByUsername(userDetails.getUsername());

        // Kiem tra quyen
        if (!post.getAuthor().getId().equals(user.getId())) {
            return "redirect:/posts/" + id + "?error=forbidden";
        }

        PostRequest postRequest = new PostRequest();
        postRequest.setTitle(post.getTitle());
        postRequest.setContent(post.getContent());
        postRequest.setTopicId(post.getTopic().getId());
        postRequest.setImageUrl(post.getImageUrl());

        model.addAttribute("postRequest", postRequest);
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("postId", id);
        return "forum/post-form";
    }

    @PostMapping("/{id}/update")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute("postRequest") PostRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("postId", id);
            return "forum/post-form";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            postService.updateOwnPost(id, request, user);
            redirectAttributes.addFlashAttribute("successMsg", "Cap nhat bai viet thanh cong!");
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("postId", id);
            model.addAttribute("errorMsg", e.getMessage());
            return "forum/post-form";
        }
    }

    // ---- Xoa mem bai viet ----

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            postService.softDeleteOwnPost(id, user);
            redirectAttributes.addFlashAttribute("successMsg", "Da xoa bai viet.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/posts/" + id;
        }
    }
}
