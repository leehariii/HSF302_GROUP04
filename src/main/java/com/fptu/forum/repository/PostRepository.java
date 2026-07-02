package com.fptu.forum.repository;

import com.fptu.forum.entity.Post;
import com.fptu.forum.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository cho entity Post.
 * Cac query JPQL tuy chinh cho cac chuc nang phuc tap.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    // Tim bai viet ACTIVE theo topic, uu tien pinned truoc, moi nhat truoc
    @Query("SELECT p FROM Post p WHERE p.topic.id = :topicId AND p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    List<Post> findActiveByTopicId(@Param("topicId") Long topicId);

    // Tim bai viet ACTIVE theo topic voi phan trang
    @Query("SELECT p FROM Post p WHERE p.topic.id = :topicId AND p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findActiveByTopicId(@Param("topicId") Long topicId, Pageable pageable);

    // Trang chu: bai viet ACTIVE moi nhat (co pin len dau)
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    // Tim kiem bai viet theo title hoac content (chi tim trong ACTIVE)
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchActivePosts(@Param("keyword") String keyword, Pageable pageable);

    // Tang view_count len 1 (su dung JPQL UPDATE)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Lay bai viet cua mot user (cho profile)
    @Query("SELECT p FROM Post p WHERE p.author.id = :userId AND p.status = 'ACTIVE' " +
           "ORDER BY p.createdAt DESC")
    List<Post> findActiveByAuthorId(@Param("userId") Long userId);

    // Moderator: lay tat ca bai viet (khong loc status) de kiem duyet
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllPosts(Pageable pageable);
}
