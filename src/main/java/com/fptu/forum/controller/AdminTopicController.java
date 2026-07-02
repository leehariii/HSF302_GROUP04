package com.fptu.forum.controller;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.service.AdminTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller cho Admin quan ly Topic.
 * URL: /admin/topics/**
 * Giu nguyen convention URL hien tai (edit/{id}, delete/{id}).
 */
@Controller
@RequestMapping("/admin/topics")
@RequiredArgsConstructor
public class AdminTopicController {

    private final AdminTopicService adminTopicService;

    // ---- Danh sach topic ----

    @GetMapping
    public String listTopics(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "15") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "createdAt") String sort,
                             @RequestParam(defaultValue = "desc") String dir) {
        Sort sortObj = dir.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();

        model.addAttribute("topics",  adminTopicService.getTopics(keyword, PageRequest.of(page, size, sortObj)));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort",    sort);
        model.addAttribute("dir",     dir);
        return "admin/topics";
    }

    // ---- Create form ----

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("topicRequest", new TopicRequest());
        model.addAttribute("isEdit", false);
        return "admin/topic-form";
    }

    // ---- Create submit ----

    @PostMapping("/create")
    public String createTopic(@Valid @ModelAttribute("topicRequest") TopicRequest request,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/topic-form";
        }
        try {
            adminTopicService.createTopic(request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Tạo chủ đề thành công.");
            return "redirect:/admin/topics";
        } catch (BusinessException e) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/topic-form";
        }
    }

    // ---- Edit form ----

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            var topic = adminTopicService.getTopicById(id);
            TopicRequest request = new TopicRequest();
            request.setName(topic.getName());
            request.setDescription(topic.getDescription());
            model.addAttribute("topicRequest", request);
            model.addAttribute("topicId",      id);
            model.addAttribute("isEdit",        true);
            return "admin/topic-form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/topics";
        }
    }

    // ---- Edit submit ----

    @PostMapping("/edit/{id}")
    public String updateTopic(@PathVariable Long id,
                              @Valid @ModelAttribute("topicRequest") TopicRequest request,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("topicId", id);
            model.addAttribute("isEdit",  true);
            return "admin/topic-form";
        }
        try {
            adminTopicService.updateTopic(id, request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật chủ đề thành công.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/topics";
    }

    // ---- Delete ----

    @PostMapping("/delete/{id}")
    public String deleteTopic(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            adminTopicService.deleteTopic(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa chủ đề.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/topics";
    }
}
