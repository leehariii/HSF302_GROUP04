package com.fptu.forum.repository;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity User.
 * extends JpaSpecificationExecutor de ho tro filter dong (Admin search/filter).
 */
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // Tim user theo username (dung cho login / UserDetailsService)
    Optional<User> findByUsername(String username);

    // Tim user theo email
    Optional<User> findByEmail(String email);

    // Kiem tra username da ton tai chua
    boolean existsByUsername(String username);

    // Kiem tra email da ton tai chua
    boolean existsByEmail(String email);

    // Lay danh sach user theo role (Admin: xem danh sach moderator)
    List<User> findAllByRole(Role role);

    // Tim kiem user theo username chua chuoi keyword (case insensitive)
    List<User> findByUsernameContainingIgnoreCase(String keyword);

    // Count theo role (Admin dashboard)
    long countByRole(Role role);

    // Count theo status (Admin dashboard)
    long countByStatus(UserStatus status);

    // Paging theo role
    Page<User> findByRole(Role role, Pageable pageable);

    // Paging theo status
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
