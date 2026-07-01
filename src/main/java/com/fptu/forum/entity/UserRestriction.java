package com.fptu.forum.entity;

import com.fptu.forum.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity anh xa bang "user_restrictions" - han che nguoi dung.
 * Logic mute con hieu luc: is_active = true AND (end_at IS NULL OR end_at > NOW).
 *
 * Chu y: FK ten "restricted_by" trong DB (khong phai "created_by").
 */
@Entity
@Table(name = "user_restrictions")
@Getter
@Setter
@NoArgsConstructor
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nguoi bi han che
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false, length = 20)
    private RestrictionType restrictionType;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Thoi diem bat dau: map vao cot start_at (DEFAULT SYSUTCDATETIME trong DB).
     */
    @CreationTimestamp
    @Column(name = "start_at", nullable = false, updatable = false)
    private LocalDateTime startAt;

    // Null = vinh vien, co gia tri = het han vao thoi diem nay
    @Column(name = "end_at")
    private LocalDateTime endAt;

    /**
     * Moderator dat han che.
     * Map vao cot "restricted_by" trong DB.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restricted_by", nullable = false)
    private User createdBy;
}
