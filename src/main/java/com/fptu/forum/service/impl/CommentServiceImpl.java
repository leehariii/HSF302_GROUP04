package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.CommentRequest;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.CommentStatus;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.CommentRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.service.CommentService;
import com.fptu.forum.service.RestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation cua CommentService.
 * Ho tro comment goc va reply 1 cap, kiem tra mute truoc khi binh luan.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RestrictionService restrictionService;

    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    @Override
    public List<Comment> findRootComments(Long postId) {
        return commentRepository.findRootCommentsByPostId(postId);
    }

    @Override
    public List<Comment> findReplies(Long parentId) {
        return commentRepository.findRepliesByParentId(parentId);
    }

    /**
     * Tao binh luan (comment goc hoac reply 1 cap).
     * Kiem tra MUTE_COMMENT truoc khi cho phep binh luan.
     */
    @Override
    @Transactional
    public Comment createComment(Long postId, CommentRequest request, User author) {
        // Kiem tra user co bi mute comment khong
        if (restrictionService.isMutedComment(author.getId())) {
            throw new ForbiddenException(
                    "Tai khoan cua ban dang bi han che binh luan.");
        }

        // Kiem tra bai viet ton tai va ACTIVE
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new ForbiddenException("Khong the binh luan bai viet nay.");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setStatus(CommentStatus.ACTIVE);

        // Neu co parentCommentId -> la reply (chi 1 cap)
        if (request.getParentCommentId() != null) {
            Comment parent = findById(request.getParentCommentId());
            if (parent.getParentComment() != null) {
                throw new ForbiddenException("Chi duoc reply 1 cap.");
            }
            comment.setParentComment(parent);
        }

        return commentRepository.save(comment);
    }

    /**
     * Xoa mem comment (Moderator action).
     */
    @Override
    @Transactional
    public void softDeleteComment(Long commentId) {
        Comment comment = findById(commentId);
        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Override
    public long countActiveComments(Long postId) {
        return commentRepository.countActiveByPostId(postId);
    }
}
