package com.fptu.forum.repository;

import com.fptu.forum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository cho entity Comment.
 * Chi hien thi comment co status = ACTIVE.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lay comment goc (khong co parent) ACTIVE cua mot bai viet
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId " +
           "AND c.parentComment IS NULL AND c.status = 'ACTIVE' " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    // Lay reply (comment con) ACTIVE cua mot comment cha
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId " +
           "AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    // Dem so comment ACTIVE cua mot bai viet
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.status = 'ACTIVE'")
    long countActiveByPostId(@Param("postId") Long postId);
}
