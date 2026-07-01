package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Like;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.repository.LikeRepository;
import com.fptu.forum.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation cua LikeService.
 * Toggle like: insert neu chua like, delete neu da like.
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    /**
     * Toggle like bai viet.
     * @return true neu vua like, false neu vua unlike.
     */
    @Override
    @Transactional
    public boolean toggleLikePost(User user, Post post) {
        Optional<Like> existing =
                likeRepository.findByUserAndPost(user.getId(), post.getId());
        if (existing.isPresent()) {
            // Da like -> unlike (xoa)
            likeRepository.delete(existing.get());
            return false;
        } else {
            // Chua like -> like (them moi)
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
            return true;
        }
    }

    /**
     * Toggle like comment.
     * @return true neu vua like, false neu vua unlike.
     */
    @Override
    @Transactional
    public boolean toggleLikeComment(User user, Comment comment) {
        Optional<Like> existing =
                likeRepository.findByUserAndComment(user.getId(), comment.getId());
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false;
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setComment(comment);
            likeRepository.save(like);
            return true;
        }
    }

    @Override
    public long countPostLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    public long countCommentLikes(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }

    @Override
    public boolean isPostLiked(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public boolean isCommentLiked(Long userId, Long commentId) {
        return likeRepository.existsByUserIdAndCommentId(userId, commentId);
    }
}
