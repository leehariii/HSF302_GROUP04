package com.fptu.forum.controller;

import com.fptu.forum.dto.request.BanUserForm;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.service.AdminUserService;
import com.fptu.forum.service.RestrictionService;
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
 * Controller cho Admin quan ly User.
 * URL: /admin/users/**
 * Chi nhan request, goi service, dua du lieu vao model, redirect.
 * Khong co business logic.
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final RestrictionService restrictionService;

    // ---- Danh sach user ----

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "createdAt") String sort,
                            @RequestParam(defaultValue = "desc") String dir) {

        Role   roleFilter   = parseEnum(Role.class, role);
        UserStatus statusFilter = parseEnum(UserStatus.class, status);

        Sort sortObj = dir.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();

        var pageResult = adminUserService.getUsers(
                keyword, roleFilter, statusFilter,
                PageRequest.of(page, size, sortObj));

        model.addAttribute("users",    pageResult);
        model.addAttribute("keyword",  keyword);
        model.addAttribute("role",     role);
        model.addAttribute("status",   status);
        model.addAttribute("sort",     sort);
        model.addAttribute("dir",      dir);
        model.addAttribute("roles",    Role.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users";
    }

    // ---- Chi tiet user ----

    @GetMapping("/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("targetUser",    adminUserService.getUserById(id));
        model.addAttribute("restrictions",  restrictionService.findByUserId(id));
        model.addAttribute("banForm",       new BanUserForm());
        return "admin/user-detail";
    }

    // ---- Ban ----

    @PostMapping("/{id}/ban")
    public String banUser(@PathVariable Long id,
                          @Valid @ModelAttribute("banForm") BanUserForm banForm,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    bindingResult.getFieldError("reason") != null
                            ? bindingResult.getFieldError("reason").getDefaultMessage()
                            : "Dữ liệu không hợp lệ.");
            return "redirect:/admin/users/" + id;
        }
        try {
            adminUserService.banUser(id, banForm.getReason(), userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã khóa tài khoản.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ---- Unban ----

    @PostMapping("/{id}/unban")
    public String unbanUser(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            adminUserService.unbanUser(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã mở khóa tài khoản.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ---- Promote ----

    @PostMapping("/{id}/promote")
    public String promote(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            adminUserService.promoteToModerator(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã promote lên MODERATOR.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ---- Demote ----

    @PostMapping("/{id}/demote")
    public String demote(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            adminUserService.demoteToMember(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Đã demote xuống MEMBER.");
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ---- Helper ----

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
