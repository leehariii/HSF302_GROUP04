package com.fptu.forum.service.impl;

import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.repository.AuditLogRepository;
import com.fptu.forum.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation cua AuditLogService.
 * Ghi nhat ky hanh dong cua Moderator va Admin.
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Ghi mot ban ghi audit log day du.
     */
    @Override
    public void log(User actor, AuditAction action,
                    User targetUser, Post targetPost, Comment targetComment,
                    String note) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action.name());
        log.setTargetUser(targetUser);
        log.setTargetPost(targetPost);
        log.setTargetComment(targetComment);
        log.setNote(note);
        auditLogRepository.save(log);
    }

    /**
     * Ghi log hanh dong lien quan den User.
     */
    @Override
    public void logUserAction(User actor, AuditAction action, User targetUser, String note) {
        log(actor, action, targetUser, null, null, note);
    }

    /**
     * Ghi log hanh dong lien quan den Post.
     */
    @Override
    public void logPostAction(User actor, AuditAction action, Post targetPost, String note) {
        log(actor, action, null, targetPost, null, note);
    }

    /**
     * Ghi log hanh dong lien quan den Comment.
     */
    @Override
    public void logCommentAction(User actor, AuditAction action, Comment targetComment, String note) {
        log(actor, action, null, null, targetComment, note);
    }

    /**
     * Lay danh sach audit log phan trang (Admin, khong filter).
     */
    @Override
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Tim kiem / filter audit log theo actor username va/hoac action.
     */
    @Override
    public Page<AuditLog> searchLogs(String actorUsername, String action, Pageable pageable) {
        boolean hasActor  = actorUsername != null && !actorUsername.isBlank();
        boolean hasAction = action != null && !action.isBlank();

        if (hasActor && hasAction) {
            return auditLogRepository
                    .findByActionAndActorUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
                            action, actorUsername, pageable);
        }
        if (hasActor) {
            return auditLogRepository
                    .findByActorUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
                            actorUsername, pageable);
        }
        if (hasAction) {
            return auditLogRepository
                    .findByActionOrderByCreatedAtDesc(action, pageable);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
