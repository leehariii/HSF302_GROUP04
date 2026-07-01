package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.PostRequest;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.service.PostService;
import com.fptu.forum.service.RestrictionService;
import com.fptu.forum.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation cua PostService.
 * Xu ly bai viet: CRUD, pin, hide, delete, view count, mute check.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TopicService topicService;
    private final RestrictionService restrictionService;

    @Override
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }

    @Override
    public Post findActiveById(Long id) {
        Post post = findById(id);
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Bai viet khong ton tai hoac da bi an/xoa.");
        }
        return post;
    }

    @Override
    public Page<Post> findAllActive(Pageable pageable) {
        return postRepository.findAllActive(pageable);
    }

    @Override
    public List<Post> findActiveByTopic(Long topicId) {
        return postRepository.findActiveByTopicId(topicId);
    }

    @Override
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchActivePosts(keyword, pageable);
    }

    /**
     * Tao bai viet moi.
     * Kiem tra MUTE_POST truoc khi cho phep dang.
     */
    @Override
    @Transactional
    public Post createPost(PostRequest request, User author) {
        if (restrictionService.isMutedPost(author.getId())) {
            throw new ForbiddenException(
                    "Tai khoan cua ban dang bi han che dang bai.");
        }

        Topic topic = topicService.findById(request.getTopicId());

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);
        post.setTopic(topic);
        post.setStatus(PostStatus.ACTIVE);
        post.setIsPinned(false);
        post.setViewCount(0);

        return postRepository.save(post);
    }

    /**
     * Sua bai viet cua chinh minh.
     * Chi cho sua khi la chu bai va status = ACTIVE.
     */
    @Override
    @Transactional
    public Post updateOwnPost(Long postId, PostRequest request, User currentUser) {
        Post post = findById(postId);

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Ban khong co quyen sua bai viet nay.");
        }
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new ForbiddenException(
                    "Khong the sua bai viet da bi an hoac xoa.");
        }

        Topic topic = topicService.findById(request.getTopicId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTopic(topic);

        return postRepository.save(post);
    }

    /**
     * Xoa mem bai viet cua chinh minh (soft delete: status = DELETED).
     */
    @Override
    @Transactional
    public void softDeleteOwnPost(Long postId, User currentUser) {
        Post post = findById(postId);

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Ban khong co quyen xoa bai viet nay.");
        }
        if (post.getStatus() == PostStatus.DELETED) {
            throw new IllegalStateException("Bai viet da duoc xoa truoc do.");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void increaseViewCount(Long postId) {
        postRepository.incrementViewCount(postId);
    }

    // ---- Moderator actions ----

    @Override
    @Transactional
    public void hidePost(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.HIDDEN);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void pinPost(Long postId) {
        Post post = findById(postId);
        post.setIsPinned(true);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void unpinPost(Long postId) {
        Post post = findById(postId);
        post.setIsPinned(false);
        postRepository.save(post);
    }
}
