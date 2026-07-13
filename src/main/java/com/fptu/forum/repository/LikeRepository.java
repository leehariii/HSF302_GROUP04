package com.fptu.forum.repository;

import com.fptu.forum.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Like.
 * Toggle like: kiem tra ton tai -> insert hoac delete.
 */
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Kiem tra user da like bai viet chua
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
    Optional<Like> findByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    // Kiem tra user da like comment chua
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.comment.id = :commentId")
    Optional<Like> findByUserAndComment(@Param("userId") Long userId, @Param("commentId") Long commentId);

    // Dem so like cua mot bai viet
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    // Dem so like cua mot comment
    @Query("SELECT COUNT(l) FROM Like l WHERE l.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);

    // Kiem tra co like khong (tra ve boolean)
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    // Batch: lay danh sach commentId ma user da like (1 query IN)
    @Query("SELECT l.comment.id FROM Like l WHERE l.user.id = :userId AND l.comment.id IN :commentIds")
    List<Long> findLikedCommentIdsByUser(@Param("userId") Long userId,
                                         @Param("commentIds") List<Long> commentIds);

    // Batch: dem so like theo nhieu commentId cung luc (1 query IN)
    // Tra ve List<Object[]> voi moi phan tu la [commentId, likeCount]
    @Query("SELECT l.comment.id, COUNT(l) FROM Like l WHERE l.comment.id IN :commentIds GROUP BY l.comment.id")
    List<Object[]> countLikesByCommentIds(@Param("commentIds") List<Long> commentIds);
}
