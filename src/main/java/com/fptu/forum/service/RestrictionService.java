package com.fptu.forum.service;

import com.fptu.forum.entity.UserRestriction;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.RestrictionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface cho RestrictionService.
 * Implementation: RestrictionServiceImpl.
 */
public interface RestrictionService {

    boolean isMutedPost(Long userId);

    boolean isMutedComment(Long userId);

    UserRestriction mutePost(User targetUser, User moderator, String reason, LocalDateTime endAt);

    UserRestriction muteComment(User targetUser, User moderator, String reason, LocalDateTime endAt);

    void unmute(Long userId, RestrictionType type);

    void unmuteAll(Long userId);

    List<UserRestriction> findByUserId(Long userId);

    UserRestriction findById(Long id);
}
