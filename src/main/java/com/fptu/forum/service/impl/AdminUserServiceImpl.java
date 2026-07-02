package com.fptu.forum.service.impl;

import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.AuditLogRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.AdminUserService;
import com.fptu.forum.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation cua AdminUserService.
 * Toan bo thao tac thay doi du lieu dung @Transactional.
 * Audit log duoc ghi trong cung transaction voi update user.
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // ---- Helper ----

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private User loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user: " + username));
    }

    private void saveAuditLog(User actor, AuditAction action, User targetUser, String note) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action.name());
        log.setTargetUser(targetUser);
        log.setNote(note);
        auditLogRepository.save(log);
    }

    // ---- Search / filter ----

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        return userRepository.findAll(
                UserSpecification.filter(keyword, role, status),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return loadUser(id);
    }

    // ---- Ban ----

    @Override
    @Transactional
    public void banUser(Long userId, String reason, String currentUsername) {
        User admin  = loadUserByUsername(currentUsername);
        User target = loadUser(userId);

        // Business rules
        if (target.getId().equals(admin.getId())) {
            throw new BusinessException("Không thể tự ban chính mình.");
        }
        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException("Không thể ban tài khoản Admin.");
        }
        if (target.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Tài khoản này đã bị khóa rồi.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Lý do ban không được để trống.");
        }

        target.setStatus(UserStatus.BANNED);
        target.setBannedReason(reason.trim());
        target.setBannedBy(admin);
        target.setBannedAt(LocalDateTime.now());
        userRepository.save(target);

        // Audit log trong cung transaction
        saveAuditLog(admin, AuditAction.BAN_USER, target,
                "Lý do: " + reason.trim());
    }

    // ---- Unban ----

    @Override
    @Transactional
    public void unbanUser(Long userId, String currentUsername) {
        User admin  = loadUserByUsername(currentUsername);
        User target = loadUser(userId);

        if (target.getStatus() != UserStatus.BANNED) {
            throw new BusinessException("Tài khoản này chưa bị khóa.");
        }

        target.setStatus(UserStatus.ACTIVE);
        target.setBannedReason(null);
        target.setBannedBy(null);
        target.setBannedAt(null);
        userRepository.save(target);

        saveAuditLog(admin, AuditAction.UNBAN_USER, target, null);
    }

    // ---- Promote ----

    @Override
    @Transactional
    public void promoteToModerator(Long userId, String currentUsername) {
        User admin  = loadUserByUsername(currentUsername);
        User target = loadUser(userId);

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException("Không thể promote chính mình.");
        }
        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException("Không thể promote tài khoản Admin.");
        }
        if (target.getRole() == Role.MODERATOR) {
            throw new BusinessException("Người dùng này đã là MODERATOR.");
        }
        if (target.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("Không thể promote tài khoản đang bị khóa.");
        }
        // chi MEMBER + ACTIVE moi duoc promote
        if (target.getRole() != Role.MEMBER) {
            throw new BusinessException("Chỉ có thể promote MEMBER lên MODERATOR.");
        }

        target.setRole(Role.MODERATOR);
        userRepository.save(target);

        saveAuditLog(admin, AuditAction.PROMOTE_MODERATOR, target,
                target.getUsername() + " duoc promote len MODERATOR");
    }

    // ---- Demote ----

    @Override
    @Transactional
    public void demoteToMember(Long userId, String currentUsername) {
        User admin  = loadUserByUsername(currentUsername);
        User target = loadUser(userId);

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException("Không thể demote chính mình.");
        }
        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException("Không thể demote tài khoản Admin.");
        }
        if (target.getRole() != Role.MODERATOR) {
            throw new BusinessException("Người dùng này không phải MODERATOR.");
        }

        target.setRole(Role.MEMBER);
        userRepository.save(target);

        saveAuditLog(admin, AuditAction.DEMOTE_MODERATOR, target,
                target.getUsername() + " bi demote xuong MEMBER");
    }
}
