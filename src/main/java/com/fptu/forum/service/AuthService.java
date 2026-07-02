package com.fptu.forum.service;

import com.fptu.forum.dto.request.RegisterRequest;
import com.fptu.forum.entity.User;

/**
 * Interface cho AuthService.
 * Implementation: AuthServiceImpl.
 */
public interface AuthService {

    /**
     * Dang ky tai khoan moi.
     */
    User register(RegisterRequest request);

    /**
     * Doi mat khau (user da dang nhap).
     */
    void changePassword(User user, String currentPassword, String newPassword);

    /**
     * Dat lai mat khau qua email (khong gui mail that).
     * Tim user theo email, kiem tra ton tai, ma hoa BCrypt roi luu.
     */
    void forgotPassword(String email, String newPassword);
}
