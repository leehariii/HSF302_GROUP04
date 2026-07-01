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
     * Doi mat khau.
     */
    void changePassword(User user, String currentPassword, String newPassword);
}
