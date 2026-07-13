package com.fptu.forum.repository;

import com.fptu.forum.entity.SavedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity SavedPost (bookmark).
 */
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    // Kiem tra user da bookmark bai viet nay chua
    @Query("SELECT sp FROM SavedPost sp WHERE sp.user.id = :userId AND sp.post.id = :postId")
    Optional<SavedPost> findByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    // Lay danh sach bookmark cua user, moi nhat truoc (List)
    @Query("SELECT sp FROM SavedPost sp WHERE sp.user.id = :userId " +
           "AND sp.post.status = 'ACTIVE' ORDER BY sp.createdAt DESC")
    List<SavedPost> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Lay danh sach bookmark cua user, co phan trang
    @Query("SELECT sp FROM SavedPost sp WHERE sp.user.id = :userId " +
           "AND sp.post.status = 'ACTIVE' ORDER BY sp.createdAt DESC")
    Page<SavedPost> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Kiem tra co bookmark khong
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
