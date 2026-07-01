package com.fptu.forum.service.impl;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation cua UserService.
 * Quan ly user (Admin): ban/unban, promote/demote, xem danh sach.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay user: " + username));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Ban user: doi status sang BANNED.
     * Khong cho Admin tu ban chinh minh hoac ban Admin khac.
     */
    @Override
    @Transactional
    public void banUser(Long targetUserId, User actorUser) {
        User target = findById(targetUserId);

        if (target.getId().equals(actorUser.getId())) {
            throw new ForbiddenException("Khong the tu ban chinh minh.");
        }
        if (target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Khong the ban tai khoan Admin khac.");
        }

        target.setStatus(UserStatus.BANNED);
        userRepository.save(target);
    }

    /**
     * Unban user: doi status sang ACTIVE.
     */
    @Override
    @Transactional
    public void unbanUser(Long targetUserId) {
        User target = findById(targetUserId);
        target.setStatus(UserStatus.ACTIVE);
        userRepository.save(target);
    }

    /**
     * Promote MEMBER len MODERATOR.
     */
    @Override
    @Transactional
    public void promoteToModerator(Long targetUserId) {
        User target = findById(targetUserId);
        if (target.getRole() != Role.MEMBER) {
            throw new IllegalArgumentException(
                    "Chi co the promote MEMBER len MODERATOR.");
        }
        target.setRole(Role.MODERATOR);
        userRepository.save(target);
    }

    /**
     * Demote MODERATOR xuong MEMBER.
     * Khong cho demote ADMIN.
     */
    @Override
    @Transactional
    public void demoteModerator(Long targetUserId) {
        User target = findById(targetUserId);
        if (target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Khong the demote tai khoan Admin.");
        }
        if (target.getRole() != Role.MODERATOR) {
            throw new IllegalArgumentException("User nay khong phai MODERATOR.");
        }
        target.setRole(Role.MEMBER);
        userRepository.save(target);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingIgnoreCase(keyword);
    }
}
