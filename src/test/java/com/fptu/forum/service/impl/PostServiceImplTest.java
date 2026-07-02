package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.enums.Role;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private User author;
    private User moderator;
    private User otherMember;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);
        author.setUsername("author");
        author.setRole(Role.MEMBER);

        moderator = new User();
        moderator.setId(2L);
        moderator.setUsername("mod");
        moderator.setRole(Role.MODERATOR);

        otherMember = new User();
        otherMember.setId(3L);
        otherMember.setUsername("other");
        otherMember.setRole(Role.MEMBER);

        post = new Post();
        post.setId(100L);
        post.setAuthor(author);
    }

    @Test
    void testGetPostForDetailView_ActivePost_AnyoneCanView() {
        post.setStatus(PostStatus.ACTIVE);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        // View as guest
        Post result = postService.getPostForDetailView(100L, null);
        assertNotNull(result);

        // View as other member
        result = postService.getPostForDetailView(100L, otherMember);
        assertNotNull(result);
    }

    @Test
    void testGetPostForDetailView_HiddenPost_AuthorCanView() {
        post.setStatus(PostStatus.HIDDEN);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        Post result = postService.getPostForDetailView(100L, author);
        assertNotNull(result);
    }

    @Test
    void testGetPostForDetailView_HiddenPost_ModeratorCanView() {
        post.setStatus(PostStatus.HIDDEN);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        Post result = postService.getPostForDetailView(100L, moderator);
        assertNotNull(result);
    }

    @Test
    void testGetPostForDetailView_HiddenPost_OtherMemberCannotView() {
        post.setStatus(PostStatus.HIDDEN);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostForDetailView(100L, otherMember);
        });
    }

    @Test
    void testGetPostForDetailView_HiddenPost_GuestCannotView() {
        post.setStatus(PostStatus.HIDDEN);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostForDetailView(100L, null);
        });
    }

    @Test
    void testGetPostForDetailView_DeletedPost_ModeratorCanView() {
        post.setStatus(PostStatus.DELETED);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        Post result = postService.getPostForDetailView(100L, moderator);
        assertNotNull(result);
    }
}
