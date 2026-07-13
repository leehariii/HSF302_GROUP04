package com.fptu.forum.repository;

import com.fptu.forum.entity.Report;
import com.fptu.forum.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository cho entity Report.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Lay danh sach report theo trang thai (Moderator xem PENDING)
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    // Dem so report PENDING (hien thi badge cho moderator)
    long countByStatus(ReportStatus status);

    // Kiem tra user da co report PENDING cho bai viet nay chua (tranh spam bao cao)
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter.id = :userId AND r.post.id = :postId AND r.status = :status")
    boolean existsByReporterAndPostAndStatus(@Param("userId") Long userId, @Param("postId") Long postId, @Param("status") ReportStatus status);

    // Kiem tra user da co report PENDING cho comment nay chua
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter.id = :userId AND r.comment.id = :commentId AND r.status = :status")
    boolean existsByReporterAndCommentAndStatus(@Param("userId") Long userId, @Param("commentId") Long commentId, @Param("status") ReportStatus status);
}
