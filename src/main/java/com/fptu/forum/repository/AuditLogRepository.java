package com.fptu.forum.repository;

import com.fptu.forum.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository cho entity AuditLog.
 * extends JpaSpecificationExecutor de ho tro search/filter dong.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {

    // Lay audit log moi nhat (Admin xem lich su)
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Lay audit log cua mot actor cu the
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);

    // Filter theo action string
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    // Filter theo actor username (ignore case)
    Page<AuditLog> findByActorUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
            String actorUsername, Pageable pageable);

    // Filter theo action VA actor username
    Page<AuditLog> findByActionAndActorUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
            String action, String actorUsername, Pageable pageable);
}
