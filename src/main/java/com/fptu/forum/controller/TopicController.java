package com.fptu.forum.controller;

import com.fptu.forum.entity.Topic;
import com.fptu.forum.service.PostService;
import com.fptu.forum.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String topicDetail(@PathVariable Long id, Model model) {
        Topic topic = topicService.findById(id);
        model.addAttribute("topic", topic);
        model.addAttribute("posts", postService.findActiveByTopic(id));
        model.addAttribute("allTopics", topicService.findAll());
        return "forum/topic-detail";
    }
}
