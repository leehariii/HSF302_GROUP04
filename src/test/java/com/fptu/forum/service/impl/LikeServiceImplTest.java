package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Like;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.repository.LikeRepository;
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
public class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        post = new Post();
        post.setId(10L);
        // Phai set ACTIVE de cac test toggle binh thuong khong bi throw exception
        post.setStatus(PostStatus.ACTIVE);
    }

    @Test
    void testToggleLikePost_FirstTime_Inserts() {
        // Arrange
        when(likeRepository.findByUserAndPost(user.getId(), post.getId())).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        boolean result = likeService.toggleLikePost(user, post);

        // Assert
        assertTrue(result, "Expected toggle to return true (liked)");
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void testToggleLikePost_SecondTime_Deletes() {
        // Arrange
        Like existingLike = new Like();
        existingLike.setUser(user);
        existingLike.setPost(post);

        when(likeRepository.findByUserAndPost(user.getId(), post.getId())).thenReturn(Optional.of(existingLike));

        // Act
        boolean result = likeService.toggleLikePost(user, post);

        // Assert
        assertFalse(result, "Expected toggle to return false (unliked)");
        verify(likeRepository, times(1)).delete(existingLike);
        verify(likeRepository, never()).save(any(Like.class));
    }

    // ---- Test case moi: kiem tra bao ve status ----

    @Test
    void testToggleLikePost_HiddenPost_ThrowsBusinessException() {
        // Arrange: bai viet bi AN
        post.setStatus(PostStatus.HIDDEN);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> likeService.toggleLikePost(user, post),
                "Phai throw BusinessException khi post HIDDEN"
        );
        assertTrue(ex.getMessage().contains("an hoac xoa"),
                "Message phai neu ro ly do bi an/xoa");
        // Dam bao khong co query nao duoc thuc hien
        verifyNoInteractions(likeRepository);
    }

    @Test
    void testToggleLikePost_DeletedPost_ThrowsBusinessException() {
        // Arrange: bai viet da bi XOA
        post.setStatus(PostStatus.DELETED);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> likeService.toggleLikePost(user, post),
                "Phai throw BusinessException khi post DELETED"
        );
        verifyNoInteractions(likeRepository);
    }
}
