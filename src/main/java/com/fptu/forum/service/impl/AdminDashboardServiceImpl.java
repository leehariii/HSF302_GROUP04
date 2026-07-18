package com.fptu.forum.service.impl;

import com.fptu.forum.dto.AdminDashboardDTO;
import com.fptu.forum.enums.ReportStatus;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.repository.CommentRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.ReportRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation cua AdminDashboardService.
 * Su dung repository.count() thay vi load data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    @Override
    public AdminDashboardDTO getStats() {
        return AdminDashboardDTO.builder()
                .totalUsers(userRepository.count())
                .totalMembers(userRepository.countByRole(Role.MEMBER))
                .totalModerators(userRepository.countByRole(Role.MODERATOR))
                .totalPosts(postRepository.count())
                .totalComments(commentRepository.count())
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .bannedUsers(userRepository.countByStatus(UserStatus.BANNED))
                .build();
    }
}
