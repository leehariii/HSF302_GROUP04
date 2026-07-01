package com.fptu.forum.controller;

import com.fptu.forum.dto.request.ChangePasswordRequest;
import com.fptu.forum.dto.request.RegisterRequest;
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
 * Controller xu ly dang ky, dang nhap, dang xuat, doi mat khau, profile.
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
            model.addAttribute("errorMsg", "Username/password khong chinh xac hoac tai khoan bi khoa.");
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
}
