package com.fptu.forum.controller;

import com.fptu.forum.entity.Topic;
import com.fptu.forum.service.PostService;
import com.fptu.forum.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller hien thi danh sach topic va bai viet theo topic.
 */
@Controller
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final PostService postService;

    @GetMapping
    public String listTopics(Model model) {
        model.addAttribute("topics", topicService.findAll());
        return "forum/topics";
    }

    @GetMapping("/{id}")
    public String topicDetail(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Topic topic = topicService.findById(id);
        model.addAttribute("topic", topic);
        model.addAttribute("posts", postService.findAllActive(id, pageable));
        model.addAttribute("allTopics", topicService.findAll());
        return "forum/topic-detail";
    }
}
