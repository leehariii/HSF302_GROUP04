package com.fptu.forum.service;

import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Interface cho LikeService.
 * Implementation: LikeServiceImpl.
 */
public interface LikeService {

    /**
     * Toggle like bai viet.
     * @return true neu vua like, false neu vua unlike.
     */
    boolean toggleLikePost(User user, Post post);

    /**
     * Toggle like comment.
     * @return true neu vua like, false neu vua unlike.
     */
    boolean toggleLikeComment(User user, Comment comment);

    long countPostLikes(Long postId);

    long countCommentLikes(Long commentId);

    boolean isPostLiked(Long userId, Long postId);

    boolean isCommentLiked(Long userId, Long commentId);

    /**
     * Batch: tra ve Map<commentId, true/false> cho user hien tai.
     * Dung 1 query IN thay vi query tung comment mot.
     */
    Map<Long, Boolean> getCommentLikedMap(Long userId, List<Long> commentIds);

    /**
     * Batch: tra ve Map<commentId, likeCount> cho nhieu comment.
     * Dung 1 query GROUP BY thay vi query tung comment mot.
     */
    Map<Long, Long> getCommentLikeCountMap(List<Long> commentIds);
}
