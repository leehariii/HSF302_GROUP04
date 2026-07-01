package com.fptu.forum.service;

import com.fptu.forum.dto.request.ReportRequest;
import com.fptu.forum.entity.Report;
import com.fptu.forum.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface cho ReportService.
 * Implementation: ReportServiceImpl.
 */
public interface ReportService {

    Page<Report> findPendingReports(Pageable pageable);

    long countPending();

    Report findById(Long id);

    Report createReportPost(Long postId, ReportRequest request, User reporter);

    Report createReportComment(Long commentId, ReportRequest request, User reporter);

    /**
     * Resolve report (vi pham).
     * @param postAction "HIDE" hoac "DELETE" neu la post, null neu la comment.
     */
    void resolveReport(Long reportId, User moderator, String postAction);

    void rejectReport(Long reportId, User moderator);
}
