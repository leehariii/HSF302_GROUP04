package com.fptu.forum.repository;

import com.fptu.forum.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho entity AuditLog.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Lay audit log moi nhat (Admin xem lich su)
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Lay audit log cua mot actor cu the
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
}
