package com.fptu.forum.service.impl;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.SavedPost;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.repository.SavedPostRepository;
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
public class SavedPostServiceImplTest {

    @Mock
    private SavedPostRepository savedPostRepository;

    @InjectMocks
    private SavedPostServiceImpl savedPostService;

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
    void testToggleSave_FirstTime_Inserts() {
        // Arrange
        when(savedPostRepository.findByUserAndPost(user.getId(), post.getId())).thenReturn(Optional.empty());
        when(savedPostRepository.save(any(SavedPost.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        boolean result = savedPostService.toggleSave(user, post);

        // Assert
        assertTrue(result);
        verify(savedPostRepository, times(1)).save(any(SavedPost.class));
        verify(savedPostRepository, never()).delete(any(SavedPost.class));
    }

    @Test
    void testToggleSave_SecondTime_Deletes() {
        // Arrange
        SavedPost existing = new SavedPost();
        existing.setUser(user);
        existing.setPost(post);

        when(savedPostRepository.findByUserAndPost(user.getId(), post.getId())).thenReturn(Optional.of(existing));

        // Act
        boolean result = savedPostService.toggleSave(user, post);

        // Assert
        assertFalse(result);
        verify(savedPostRepository, times(1)).delete(existing);
        verify(savedPostRepository, never()).save(any(SavedPost.class));
    }

    // ---- Test case moi: kiem tra bao ve status ----

    @Test
    void testToggleSave_HiddenPost_ThrowsBusinessException() {
        // Arrange: bai viet bi AN
        post.setStatus(PostStatus.HIDDEN);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> savedPostService.toggleSave(user, post),
                "Phai throw BusinessException khi post HIDDEN"
        );
        assertTrue(ex.getMessage().contains("an hoac xoa"),
                "Message phai neu ro ly do bi an/xoa");
        // Dam bao khong co query nao duoc thuc hien
        verifyNoInteractions(savedPostRepository);
    }

    @Test
    void testToggleSave_DeletedPost_ThrowsBusinessException() {
        // Arrange: bai viet da bi XOA
        post.setStatus(PostStatus.DELETED);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> savedPostService.toggleSave(user, post),
                "Phai throw BusinessException khi post DELETED"
        );
        verifyNoInteractions(savedPostRepository);
    }
}
