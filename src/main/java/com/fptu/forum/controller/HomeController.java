package com.fptu.forum.controller;

import com.fptu.forum.service.PostService;
import com.fptu.forum.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller trang chu va tim kiem.
 * Public: ai cung co the xem.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final TopicService topicService;

    @GetMapping({"/", "/home"})
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("posts", postService.findAllActive(pageable));
        model.addAttribute("topics", topicService.findAll());
        return "forum/home";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("posts", postService.searchPosts(keyword.trim(), pageable));
            model.addAttribute("keyword", keyword);
        }
        return "forum/home";
    }
}
