package com.fptu.forum.repository;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity User.
 * Spring Data JPA tu dong tao SQL tu ten phuong thuc.
 */
public interface UserRepository extends JpaRepository<User, Long> {

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
}
