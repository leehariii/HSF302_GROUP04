package com.fptu.forum.service;

import com.fptu.forum.dto.request.CommentRequest;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.User;

import java.util.List;

/**
 * Interface cho CommentService.
 * Implementation: CommentServiceImpl.
 */
public interface CommentService {

    Comment findById(Long id);

    List<Comment> findRootComments(Long postId);

    List<Comment> findReplies(Long parentId);

    Comment createComment(Long postId, CommentRequest request, User author);

    void softDeleteComment(Long commentId);

    void deleteMyComment(Long commentId, User user);

    long countActiveComments(Long postId);
}
