package com.fptu.forum.service.impl;

import com.fptu.forum.entity.User;
import com.fptu.forum.entity.UserRestriction;
import com.fptu.forum.enums.RestrictionType;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.UserRestrictionRepository;
import com.fptu.forum.service.RestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation cua RestrictionService.
 * Mute/unmute user, kiem tra restriction con hieu luc.
 *
 * Logic mute con hieu luc:
 *   is_active = true AND (end_at IS NULL OR end_at > NOW)
 */
@Service
@RequiredArgsConstructor
public class RestrictionServiceImpl implements RestrictionService {

    private final UserRestrictionRepository restrictionRepository;

    /**
     * Kiem tra user co dang bi mute dang bai khong.
     */
    @Override
    public boolean isMutedPost(Long userId) {
        return restrictionRepository.isUserMuted(
                userId, RestrictionType.MUTE_POST, LocalDateTime.now());
    }

    /**
     * Kiem tra user co dang bi mute binh luan khong.
     */
    @Override
    public boolean isMutedComment(Long userId) {
        return restrictionRepository.isUserMuted(
                userId, RestrictionType.MUTE_COMMENT, LocalDateTime.now());
    }

    /**
     * Mute user khong cho dang bai.
     */
    @Override
    @Transactional
    public UserRestriction mutePost(User targetUser, User moderator,
                                    String reason, LocalDateTime endAt) {
        return createRestriction(targetUser, moderator,
                RestrictionType.MUTE_POST, reason, endAt);
    }

    /**
     * Mute user khong cho binh luan.
     */
    @Override
    @Transactional
    public UserRestriction muteComment(User targetUser, User moderator,
                                       String reason, LocalDateTime endAt) {
        return createRestriction(targetUser, moderator,
                RestrictionType.MUTE_COMMENT, reason, endAt);
    }

    private UserRestriction createRestriction(User targetUser, User moderator,
                                               RestrictionType type,
                                               String reason, LocalDateTime endAt) {
        UserRestriction restriction = new UserRestriction();
        restriction.setUser(targetUser);
        restriction.setCreatedBy(moderator);
        restriction.setRestrictionType(type);
        restriction.setReason(reason);
        restriction.setIsActive(true);
        restriction.setEndAt(endAt);
        return restrictionRepository.save(restriction);
    }

    /**
     * Unmute user theo loai restriction:
     * Tat tat ca restriction ACTIVE cua user theo loai.
     */
    @Override
    @Transactional
    public void unmute(Long userId, RestrictionType type) {
        List<UserRestriction> actives = restrictionRepository.findActiveRestrictions(
                userId, type, LocalDateTime.now());
        for (UserRestriction r : actives) {
            r.setIsActive(false);
            restrictionRepository.save(r);
        }
    }

    /**
     * Unmute tat ca (ca MUTE_POST va MUTE_COMMENT).
     */
    @Override
    @Transactional
    public void unmuteAll(Long userId) {
        unmute(userId, RestrictionType.MUTE_POST);
        unmute(userId, RestrictionType.MUTE_COMMENT);
    }

    @Override
    public List<UserRestriction> findByUserId(Long userId) {
        return restrictionRepository.findByUserIdOrderByStartAtDesc(userId);
    }

    @Override
    public UserRestriction findById(Long id) {
        return restrictionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", id));
    }
}
