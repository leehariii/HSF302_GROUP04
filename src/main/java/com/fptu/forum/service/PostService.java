package com.fptu.forum.service;

import com.fptu.forum.dto.request.PostRequest;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface cho PostService.
 * Implementation: PostServiceImpl.
 */
public interface PostService {

    Post findById(Long id);

    Post findActiveById(Long id);

    Post getPostForDetailView(Long id, User currentUser);

    Page<Post> findAllActive(Long topicId, Pageable pageable);

    List<Post> findActiveByTopic(Long topicId);

    Page<Post> searchPosts(String keyword, Long topicId, Pageable pageable);

    Post createPost(PostRequest request, User author);

    Post updateOwnPost(Long postId, PostRequest request, User currentUser);

    void softDeleteOwnPost(Long postId, User currentUser);

    void increaseViewCount(Long postId);

    // Moderator actions
    void hidePost(Long postId);

    void deletePost(Long postId);

    void pinPost(Long postId);

    void unpinPost(Long postId);
}
