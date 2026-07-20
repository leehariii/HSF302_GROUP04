package com.fptu.forum;

import com.fptu.forum.dto.request.RegisterRequest;
import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.PostStatus;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.TopicRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostDetailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TopicRepository topicRepository;

    private Long testPostId;
    private Long deletedPostId;
    private User memberA;
    private User mod;
    private User memberB;

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        
        // 1. Create Topic
        Topic topic = new Topic();
        topic.setName("Test Topic " + suffix);
        topic.setDescription("Desc");
        topic = topicRepository.save(topic);

        // 2. Register Member A
        RegisterRequest reqA = new RegisterRequest();
        reqA.setUsername("memberA_" + suffix);
        reqA.setPassword("123");
        reqA.setEmail("a_" + suffix + "@a.com");
        reqA.setFullName("Member A");
        authService.register(reqA);
        memberA = userRepository.findByUsername("memberA_" + suffix).get();

        // 3. Register Member B
        RegisterRequest reqB = new RegisterRequest();
        reqB.setUsername("memberB_" + suffix);
        reqB.setPassword("123");
        reqB.setEmail("b_" + suffix + "@b.com");
        reqB.setFullName("Member B");
        authService.register(reqB);
        memberB = userRepository.findByUsername("memberB_" + suffix).get();

        // 4. Register Mod
        RegisterRequest reqMod = new RegisterRequest();
        reqMod.setUsername("mod_" + suffix);
        reqMod.setPassword("123");
        reqMod.setEmail("mod_" + suffix + "@m.com");
        reqMod.setFullName("Mod User");
        authService.register(reqMod);
        mod = userRepository.findByUsername("mod_" + suffix).get();
        mod.setRole(com.fptu.forum.enums.Role.MODERATOR);
        userRepository.save(mod);

        // 5. Create HIDDEN post by Member A
        Post post = new Post();
        post.setTitle("Secret Post " + suffix);
        post.setContent("Hidden Content");
        post.setAuthor(memberA);
        post.setTopic(topic);
        post.setStatus(PostStatus.HIDDEN);
        post.setViewCount(5); 
        post = postRepository.save(post);
        testPostId = post.getId();
        
        // 6. Create DELETED post by Member A
        Post deletedPost = new Post();
        deletedPost.setTitle("Deleted Post " + suffix);
        deletedPost.setContent("Deleted Content");
        deletedPost.setAuthor(memberA);
        deletedPost.setTopic(topic);
        deletedPost.setStatus(PostStatus.DELETED);
        deletedPost.setViewCount(10);
        deletedPost = postRepository.save(deletedPost);
        deletedPostId = deletedPost.getId();
    }

    @Test
    void testHiddenPost_MemberA_CanView_And_BannerPresent_And_NoViewIncrease() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/" + testPostId)
                .with(user(memberA.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("post"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Bài viết này đang bị ẩn hoặc xóa."), "Banner is missing");
        
        Post postInDb = postRepository.findById(testPostId).get();
        assertEquals(5, postInDb.getViewCount(), "View count should not increase for hidden post");
    }

    @Test
    void testHiddenPost_Mod_CanView_And_BannerPresent_And_NoViewIncrease() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/" + testPostId)
                .with(user(mod.getUsername()).roles("MODERATOR")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("post"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Bài viết này đang bị ẩn hoặc xóa."), "Banner is missing");
        
        Post postInDb = postRepository.findById(testPostId).get();
        assertEquals(5, postInDb.getViewCount(), "View count should not increase for hidden post");
    }

    @Test
    void testHiddenPost_MemberB_CannotView() throws Exception {
        mockMvc.perform(get("/posts/" + testPostId)
                .with(user(memberB.getUsername()).roles("MEMBER")))
                .andExpect(status().isNotFound()); 
    }
    
    @Test
    void testHiddenPost_Guest_CannotView() throws Exception {
        mockMvc.perform(get("/posts/" + testPostId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletedPost_MemberA_CanView_And_BannerPresent() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/" + deletedPostId)
                .with(user(memberA.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Bài viết này đang bị ẩn hoặc xóa."), "Banner is missing");
    }

    @Test
    void testDeletedPost_MemberB_CannotView() throws Exception {
        mockMvc.perform(get("/posts/" + deletedPostId)
                .with(user(memberB.getUsername()).roles("MEMBER")))
                .andExpect(status().isNotFound());
    }
}
