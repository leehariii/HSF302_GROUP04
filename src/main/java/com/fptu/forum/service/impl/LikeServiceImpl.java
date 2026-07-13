package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Like;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.repository.LikeRepository;
import com.fptu.forum.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // Khong cho like bai viet da HIDDEN hoac DELETED
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new BusinessException("Khong the thich bai viet nay vi bai da bi an hoac xoa.");
        }

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

    /**
     * Batch: tra ve Map<commentId, liked> cho user — 1 query IN.
     * Comment khong co trong ket qua -> mac dinh false (chua like).
     */
    @Override
    public Map<Long, Boolean> getCommentLikedMap(Long userId, List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> likedIds = likeRepository.findLikedCommentIdsByUser(userId, commentIds);
        Map<Long, Boolean> result = new HashMap<>();
        for (Long id : commentIds) {
            result.put(id, likedIds.contains(id));
        }
        return result;
    }

    /**
     * Batch: tra ve Map<commentId, likeCount> — 1 query GROUP BY.
     * Comment chua co like nao -> mac dinh 0.
     */
    @Override
    public Map<Long, Long> getCommentLikeCountMap(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> rows = likeRepository.countLikesByCommentIds(commentIds);
        Map<Long, Long> result = new HashMap<>();
        for (Long id : commentIds) {
            result.put(id, 0L); // mac dinh 0
        }
        for (Object[] row : rows) {
            Long commentId = (Long) row[0];
            Long count    = (Long) row[1];
            result.put(commentId, count);
        }
        return result;
    }
}
