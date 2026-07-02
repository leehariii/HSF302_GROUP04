package com.fptu.forum.service;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service rieng danh cho Admin quan ly User.
 * Tach khoi UserService (Auth/Profile) de tranh phuc tap hoa.
 * Moi thao tac thay doi du lieu deu @Transactional va ghi audit log
 * trong cung mot transaction.
 */
public interface AdminUserService {

    /**
     * Tim kiem + filter + phan trang user.
     * keyword: tim trong username, email, fullName.
     */
    Page<User> getUsers(String keyword, Role role, UserStatus status, Pageable pageable);

    User getUserById(Long id);

    /**
     * Ban user: dat status = BANNED, ghi bannedReason, bannedBy, bannedAt.
     * Ghi audit log trong cung transaction.
     */
    void banUser(Long userId, String reason, String currentUsername);

    /**
     * Unban user: dat status = ACTIVE, xoa bannedReason/By/At.
     * Ghi audit log trong cung transaction.
     */
    void unbanUser(Long userId, String currentUsername);

    /**
     * Promote MEMBER ACTIVE -> MODERATOR.
     * Ghi audit log trong cung transaction.
     */
    void promoteToModerator(Long userId, String currentUsername);

    /**
     * Demote MODERATOR -> MEMBER.
     * Ghi audit log trong cung transaction.
     */
    void demoteToMember(Long userId, String currentUsername);
}
