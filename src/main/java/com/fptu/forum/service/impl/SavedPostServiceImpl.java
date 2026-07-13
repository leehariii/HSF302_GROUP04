package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.SavedPost;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.repository.SavedPostRepository;
import com.fptu.forum.service.SavedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation cua SavedPostService.
 * Toggle bookmark: insert neu chua luu, delete neu da luu.
 */
@Service
@RequiredArgsConstructor
public class SavedPostServiceImpl implements SavedPostService {

    private final SavedPostRepository savedPostRepository;

    /**
     * Toggle bookmark bai viet.
     * @return true neu vua bookmark, false neu vua unbookmark.
     */
    @Override
    @Transactional
    public boolean toggleSave(User user, Post post) {
        // Khong cho luu bai viet da HIDDEN hoac DELETED
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new BusinessException("Khong the luu bai viet nay vi bai da bi an hoac xoa.");
        }

        Optional<SavedPost> existing =
                savedPostRepository.findByUserAndPost(user.getId(), post.getId());
        if (existing.isPresent()) {
            savedPostRepository.delete(existing.get());
            return false;
        } else {
            SavedPost savedPost = new SavedPost();
            savedPost.setUser(user);
            savedPost.setPost(post);
            savedPostRepository.save(savedPost);
            return true;
        }
    }

    @Override
    public List<SavedPost> listSavedPosts(Long userId) {
        return savedPostRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<SavedPost> listSavedPosts(Long userId, Pageable pageable) {
        return savedPostRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public boolean isSaved(Long userId, Long postId) {
        return savedPostRepository.existsByUserIdAndPostId(userId, postId);
    }
}
