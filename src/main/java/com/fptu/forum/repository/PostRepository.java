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

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"topic", "author"})
    java.util.Optional<Post> findById(Long id);

    // Tim bai viet ACTIVE theo topic, uu tien pinned truoc, moi nhat truoc
    @Query("SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.topic.id = :topicId AND p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    List<Post> findActiveByTopicId(@Param("topicId") Long topicId);

    // Tim bai viet ACTIVE theo topic voi phan trang
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.topic.id = :topicId AND p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.topic.id = :topicId AND p.status = 'ACTIVE'")
    Page<Post> findActiveByTopicId(@Param("topicId") Long topicId, Pageable pageable);

    // Trang chu: bai viet ACTIVE moi nhat (co pin len dau)
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'ACTIVE'")
    Page<Post> findAllActive(Pageable pageable);

    // Browse: bai viet ACTIVE voi optional topic filter, co phan trang
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.status = 'ACTIVE' " +
           "AND (:topicId IS NULL OR p.topic.id = :topicId) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'ACTIVE' " +
           "AND (:topicId IS NULL OR p.topic.id = :topicId)")
    Page<Post> findActiveByOptionalTopic(@Param("topicId") Long topicId, Pageable pageable);

    // Tim kiem bai viet theo title hoac content (chi tim trong ACTIVE)
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.status = 'ACTIVE' AND " +
           "(:topicId IS NULL OR p.topic.id = :topicId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = 'ACTIVE' AND " +
           "(:topicId IS NULL OR p.topic.id = :topicId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchActivePosts(@Param("keyword") String keyword, @Param("topicId") Long topicId, Pageable pageable);

    // Tang view_count len 1 (su dung JPQL UPDATE)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Lay bai viet cua mot user (cho profile)
    @Query("SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author " +
           "WHERE p.author.id = :userId AND p.status = 'ACTIVE' " +
           "ORDER BY p.createdAt DESC")
    List<Post> findActiveByAuthorId(@Param("userId") Long userId);

    // Moderator: lay tat ca bai viet (khong loc status) de kiem duyet
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.topic JOIN FETCH p.author ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllPosts(Pageable pageable);

    // Admin: kiem tra topic co bai viet nao khong (truoc khi xoa topic)
    boolean existsByTopicId(Long topicId);
}
