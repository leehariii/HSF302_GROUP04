package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.ReportRequest;
import com.fptu.forum.entity.*;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.enums.CommentStatus;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.enums.ReportStatus;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.CommentRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.ReportRepository;
import com.fptu.forum.service.AuditLogService;
import com.fptu.forum.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation cua ReportService.
 * Xu ly bao cao vi pham, cap nhat post/comment khi resolve, ghi audit log.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AuditLogService auditLogService;

    @Override
    public Page<Report> findPendingReports(Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(
                ReportStatus.PENDING, pageable);
    }

    @Override
    public long countPending() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    @Override
    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id));
    }

    /**
     * Tao report bai viet.
     */
    @Override
    @Transactional
    public Report createReportPost(Long postId, ReportRequest request, User reporter) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Kiem tra da co report PENDING cho bai nay chua
        if (reportRepository.existsByReporterAndPostAndStatus(reporter.getId(), postId, ReportStatus.PENDING)) {
            throw new IllegalStateException("Ban da bao cao bai viet nay roi.");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setPost(post);
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);

        return reportRepository.save(report);
    }

    /**
     * Tao report comment.
     */
    @Override
    @Transactional
    public Report createReportComment(Long commentId, ReportRequest request, User reporter) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (reportRepository.existsByReporterAndCommentAndStatus(reporter.getId(), commentId, ReportStatus.PENDING)) {
            throw new IllegalStateException("Ban da bao cao binh luan nay roi.");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setComment(comment);
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);

        return reportRepository.save(report);
    }

    /**
     * Resolve report (vi pham):
     * - Cap nhat trang thai report -> RESOLVED
     * - Xu ly post: HIDE hoac DELETE
     * - Xu ly comment: DELETED
     * - Ghi audit log
     */
    @Override
    @Transactional
    public void resolveReport(Long reportId, User moderator, String postAction) {
        Report report = findById(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("Report nay da duoc xu ly.");
        }

        // Cap nhat trang thai report
        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewer(moderator);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        // Xu ly post
        if (report.getPost() != null && postAction != null) {
            Post post = report.getPost();
            if ("HIDE".equals(postAction)) {
                post.setStatus(PostStatus.HIDDEN);
                postRepository.save(post);
                auditLogService.logPostAction(moderator, AuditAction.HIDE_POST,
                        post, "Resolve report #" + reportId);
            } else if ("DELETE".equals(postAction)) {
                post.setStatus(PostStatus.DELETED);
                postRepository.save(post);
                auditLogService.logPostAction(moderator, AuditAction.DELETE_POST,
                        post, "Resolve report #" + reportId);
            }
        }

        // Xu ly comment
        if (report.getComment() != null) {
            Comment comment = report.getComment();
            comment.setStatus(CommentStatus.DELETED);
            commentRepository.save(comment);
            auditLogService.logCommentAction(moderator, AuditAction.DELETE_COMMENT,
                    comment, "Resolve report #" + reportId);
        }

        auditLogService.log(moderator, AuditAction.RESOLVE_REPORT,
                null, report.getPost(), report.getComment(),
                "Report #" + reportId);
    }

    /**
     * Reject report (khong vi pham): ghi audit log.
     */
    @Override
    @Transactional
    public void rejectReport(Long reportId, User moderator) {
        Report report = findById(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("Report nay da duoc xu ly.");
        }

        report.setStatus(ReportStatus.REJECTED);
        report.setReviewer(moderator);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        auditLogService.log(moderator, AuditAction.REJECT_REPORT,
                null, report.getPost(), report.getComment(),
                "Report #" + reportId);
    }
}
