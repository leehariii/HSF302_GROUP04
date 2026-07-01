package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.RegisterRequest;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation cua AuthService.
 * Xu ly dang ky tai khoan va doi mat khau.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Dang ky tai khoan moi.
     * - Kiem tra username/email khong bi trung
     * - Kiem tra password == confirmPassword
     * - Encode password BCrypt
     * - Role mac dinh: MEMBER, Status mac dinh: ACTIVE
     */
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // Kiem tra username trung
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' da duoc su dung.");
        }

        // Kiem tra email trung
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email '" + request.getEmail() + "' da duoc su dung.");
        }

        // Kiem tra mat khau khop
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mat khau xac nhan khong khop.");
        }

        // Tao user moi
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.MEMBER);         // Mac dinh MEMBER
        user.setStatus(UserStatus.ACTIVE); // Mac dinh ACTIVE

        return userRepository.save(user);
    }

    /**
     * Doi mat khau.
     * Kiem tra mat khau hien tai truoc khi doi.
     */
    @Override
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ForbiddenException("Mat khau hien tai khong chinh xac.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
