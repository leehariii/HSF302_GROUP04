package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.CommentRequest;
import com.fptu.forum.entity.Comment;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.CommentStatus;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.ForbiddenException;
import com.fptu.forum.repository.CommentRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.service.RestrictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private RestrictionService restrictionService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Post post;
    private CommentRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        post = new Post();
        post.setId(10L);
        post.setStatus(PostStatus.ACTIVE);

        request = new CommentRequest();
        request.setContent("Test comment");
    }

    @Test
    void testCreateComment_Success() {
        // Arrange
        when(restrictionService.isMutedComment(user.getId())).thenReturn(false);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        // Act
        Comment result = commentService.createComment(post.getId(), request, user);

        // Assert
        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
        assertEquals(CommentStatus.ACTIVE, result.getStatus());
        assertEquals(user, result.getAuthor());
        assertEquals(post, result.getPost());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateComment_FailsWhenUserIsMuted() {
        // Arrange
        when(restrictionService.isMutedComment(user.getId())).thenReturn(true);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            commentService.createComment(post.getId(), request, user);
        });

        assertEquals("Tai khoan cua ban dang bi han che binh luan.", exception.getMessage());
        verify(postRepository, never()).findById(anyLong());
        verify(commentRepository, never()).save(any());
    }
}
