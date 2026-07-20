package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.PostRequest;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.enums.Role;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.service.RestrictionService;
import com.fptu.forum.service.TopicService;
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

    @Mock
    private TopicService topicService;

    @Mock
    private RestrictionService restrictionService;

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

    @Test
    void createPost_UserBiMutePost_NemForbiddenException() {
        PostRequest request = new PostRequest();
        when(restrictionService.isMutedPost(author.getId())).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> postService.createPost(request, author));
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_HopLe_TaoThanhCong() {
        PostRequest request = new PostRequest();
        request.setTopicId(1L);
        request.setTitle("New Post");
        request.setContent("Content");
        
        when(restrictionService.isMutedPost(author.getId())).thenReturn(false);
        Topic topic = new Topic();
        topic.setId(1L);
        when(topicService.findById(1L)).thenReturn(topic);
        
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.createPost(request, author);

        assertEquals(PostStatus.ACTIVE, result.getStatus());
        assertFalse(result.getIsPinned());
        assertEquals(0, result.getViewCount());
        assertEquals("New Post", result.getTitle());
        assertEquals("Content", result.getContent());
    }

    @Test
    void updateOwnPost_KhongPhaiChuBai_NemForbiddenException() {
        PostRequest request = new PostRequest();
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> postService.updateOwnPost(100L, request, otherMember));
        verify(postRepository, never()).save(any());
    }

    @Test
    void updateOwnPost_BaiKhongConActive_NemForbiddenException() {
        post.setStatus(PostStatus.HIDDEN);
        PostRequest request = new PostRequest();
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> postService.updateOwnPost(100L, request, author));
        verify(postRepository, never()).save(any());
    }

    @Test
    void updateOwnPost_HopLe_CapNhatThanhCong() {
        post.setStatus(PostStatus.ACTIVE);
        PostRequest request = new PostRequest();
        request.setTopicId(2L);
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        Topic topic = new Topic();
        topic.setId(2L);
        
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(topicService.findById(2L)).thenReturn(topic);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.updateOwnPost(100L, request, author);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        assertEquals(2L, result.getTopic().getId());
    }

    @Test
    void softDeleteOwnPost_KhongPhaiChuBai_NemForbiddenException() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> postService.softDeleteOwnPost(100L, otherMember));
    }

    @Test
    void softDeleteOwnPost_DaXoaTruocDo_NemIllegalStateException() {
        post.setStatus(PostStatus.DELETED);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        assertThrows(IllegalStateException.class, () -> postService.softDeleteOwnPost(100L, author));
    }

    @Test
    void softDeleteOwnPost_HopLe_ChuyenStatusThanhDeleted() {
        post.setStatus(PostStatus.ACTIVE);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.softDeleteOwnPost(100L, author);

        assertEquals(PostStatus.DELETED, post.getStatus());
    }
}
