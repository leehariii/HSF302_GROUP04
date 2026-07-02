package com.fptu.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chua so lieu thong ke cho Admin Dashboard.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    private long totalUsers;
    private long totalMembers;
    private long totalModerators;
    private long totalPosts;
    private long totalComments;
    private long pendingReports;
    private long bannedUsers;
}
