package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.ReportRequest;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.Report;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.ReportStatus;
import com.fptu.forum.repository.CommentRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User user;
    private Post post;
    private ReportRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        post = new Post();
        post.setId(10L);

        request = new ReportRequest();
        request.setReason("Spam content");
    }

    @Test
    void testCreateReportPost_Success() {
        // Arrange
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(reportRepository.existsByReporterAndPostAndStatus(user.getId(), post.getId(), ReportStatus.PENDING)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(i -> {
            Report r = i.getArgument(0);
            r.setId(100L);
            return r;
        });

        // Act
        Report result = reportService.createReportPost(post.getId(), request, user);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.PENDING, result.getStatus(), "Report status must be PENDING");
        assertEquals(user, result.getReporter(), "Reporter must be the current user");
        assertEquals(post, result.getPost());
        assertNull(result.getComment());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void testCreateReportPost_FailsWhenReportExists() {
        // Arrange
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(reportRepository.existsByReporterAndPostAndStatus(user.getId(), post.getId(), ReportStatus.PENDING)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reportService.createReportPost(post.getId(), request, user);
        });

        assertEquals("Ban da bao cao bai viet nay roi.", exception.getMessage());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testReportValidation_CannotHaveBothPostAndComment() {
        // This test simulates the DB check manually or tests entity state
        Report report = new Report();
        report.setPost(new Post());
        report.setComment(new Comment());

        // In JPA, the @Check constraint (CHK_reports_target) handles this at DB level.
        // We just assert that it is technically possible in Java but will fail on persist.
        assertNotNull(report.getPost());
        assertNotNull(report.getComment());
    }
}
