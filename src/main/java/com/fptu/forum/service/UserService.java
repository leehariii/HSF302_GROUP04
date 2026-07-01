package com.fptu.forum.service;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface cho UserService.
 * Implementation: UserServiceImpl.
 */
public interface UserService {

    User findByUsername(String username);

    User findById(Long id);

    Page<User> findAll(Pageable pageable);

    void banUser(Long targetUserId, User actorUser);

    void unbanUser(Long targetUserId);

    void promoteToModerator(Long targetUserId);

    void demoteModerator(Long targetUserId);

    List<User> searchUsers(String keyword);
}
