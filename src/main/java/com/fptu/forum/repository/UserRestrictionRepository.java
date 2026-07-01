package com.fptu.forum.repository;

import com.fptu.forum.entity.UserRestriction;
import com.fptu.forum.enums.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho entity UserRestriction.
 * Logic mute con hieu luc: is_active = true AND (end_at IS NULL OR end_at > NOW)
 */
public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {

    /**
     * Kiem tra user co dang bi mute khong (con hieu luc).
     * JPQL query theo yeu cau: isActive = true AND restrictionType = :type
     * AND user.id = :userId AND (endAt IS NULL OR endAt > :now)
     */
    @Query("SELECT COUNT(r) > 0 FROM UserRestriction r " +
           "WHERE r.user.id = :userId " +
           "AND r.restrictionType = :type " +
           "AND r.isActive = true " +
           "AND (r.endAt IS NULL OR r.endAt > :now)")
    boolean isUserMuted(@Param("userId") Long userId,
                        @Param("type") RestrictionType type,
                        @Param("now") LocalDateTime now);

    // Lay danh sach restriction cua user
    List<UserRestriction> findByUserIdOrderByStartAtDesc(Long userId);

    // Lay restriction ACTIVE cua user theo loai (de unmute)
    @Query("SELECT r FROM UserRestriction r " +
           "WHERE r.user.id = :userId " +
           "AND r.restrictionType = :type " +
           "AND r.isActive = true " +
           "AND (r.endAt IS NULL OR r.endAt > :now)")
    List<UserRestriction> findActiveRestrictions(@Param("userId") Long userId,
                                                 @Param("type") RestrictionType type,
                                                 @Param("now") LocalDateTime now);
}
