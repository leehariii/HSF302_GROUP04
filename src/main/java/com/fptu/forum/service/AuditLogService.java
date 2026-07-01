package com.fptu.forum.service;

import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface cho AuditLogService.
 * Implementation: AuditLogServiceImpl.
 */
public interface AuditLogService {

    void log(User actor, AuditAction action,
             User targetUser, Post targetPost, Comment targetComment,
             String note);

    void logUserAction(User actor, AuditAction action, User targetUser, String note);

    void logPostAction(User actor, AuditAction action, Post targetPost, String note);

    void logCommentAction(User actor, AuditAction action, Comment targetComment, String note);

    Page<AuditLog> getAllLogs(Pageable pageable);
}
