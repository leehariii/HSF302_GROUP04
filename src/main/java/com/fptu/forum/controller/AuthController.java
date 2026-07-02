package com.fptu.forum.controller;

import com.fptu.forum.dto.request.ForgotPasswordRequest;
import com.fptu.forum.dto.request.RegisterRequest;
import com.fptu.forum.service.AuthService;
import com.fptu.forum.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xu ly dang ky, dang nhap, dang xuat, quen mat khau.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // ---- Dang nhap ----

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Username/email hoac mat khau khong chinh xac, hoac tai khoan bi khoa.");
        }
        if (logout != null) {
            model.addAttribute("successMsg", "Ban da dang xuat thanh cong.");
        }
        return "auth/login";
    }

    // ---- Dang ky ----

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Dang ky thanh cong! Vui long dang nhap.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("registration.error", e.getMessage());
            return "auth/register";
        }
    }

    // ---- Quen mat khau ----

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Mat khau xac nhan khong khop.");
            return "auth/forgot-password";
        }

        try {
            authService.forgotPassword(request.getEmail(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMsg",
                    "Dat lai mat khau thanh cong! Vui long dang nhap lai.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("error", e.getMessage());
            return "auth/forgot-password";
        }
    }
}
