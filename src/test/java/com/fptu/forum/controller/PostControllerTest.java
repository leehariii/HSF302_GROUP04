package com.fptu.forum.controller;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;
    @Mock
    private TopicService topicService;
    @Mock
    private CommentService commentService;
    @Mock
    private LikeService likeService;
    @Mock
    private SavedPostService savedPostService;
    @Mock
    private UserService userService;
    @Mock
    private Model model;

    @InjectMocks
    private PostController postController;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(100L);
    }

    @Test
    void testPostDetail_ActivePost_IncreasesViewCount() {
        post.setStatus(PostStatus.ACTIVE);
        when(postService.getPostForDetailView(eq(100L), any())).thenReturn(post);

        postController.postDetail(100L, null, model);

        // Verify increaseViewCount is called because post is ACTIVE
        verify(postService, times(1)).increaseViewCount(100L);
    }

    @Test
    void testPostDetail_HiddenPost_DoesNotIncreaseViewCount() {
        post.setStatus(PostStatus.HIDDEN);
        when(postService.getPostForDetailView(eq(100L), any())).thenReturn(post);

        postController.postDetail(100L, null, model);

        // Verify increaseViewCount is NEVER called because post is HIDDEN
        verify(postService, never()).increaseViewCount(100L);
    }
}
