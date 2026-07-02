package com.fptu.forum.service;

import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;

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
}
