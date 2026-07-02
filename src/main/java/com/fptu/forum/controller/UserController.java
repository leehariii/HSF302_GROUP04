package com.fptu.forum.controller;

import com.fptu.forum.dto.request.ChangePasswordRequest;
import com.fptu.forum.dto.request.UpdateProfileRequest;
import com.fptu.forum.entity.User;
import com.fptu.forum.service.AuthService;
import com.fptu.forum.service.UserService;
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
 * Controller xu ly profile nguoi dung va doi mat khau.
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // ---- Xem profile ----

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "auth/profile";
    }

    // ---- Chinh sua profile ----

    @GetMapping("/profile/edit")
    public String editProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName(user.getFullName());
        req.setAvatarUrl(user.getAvatarUrl());
        model.addAttribute("user", user);
        model.addAttribute("updateProfileRequest", req);
        return "auth/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/profile-edit";
        }

        userService.updateProfile(user.getId(), request.getFullName(), request.getAvatarUrl());
        redirectAttributes.addFlashAttribute("successMsg", "Cap nhat profile thanh cong!");
        return "redirect:/profile";
    }

    // ---- Doi mat khau ----

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Mat khau xac nhan khong khop.");
            return "auth/change-password";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            authService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMsg", "Doi mat khau thanh cong!");
            return "redirect:/profile";
        } catch (Exception e) {
            bindingResult.reject("error", e.getMessage());
            return "auth/change-password";
        }
    }
}
