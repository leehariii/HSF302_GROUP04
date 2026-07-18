package com.fptu.forum.service;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.AuditLogRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.TopicRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.impl.AdminTopicServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AdminTopicService.
 * Test 7 ca ban quy dinh + audit log.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AdminTopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AdminTopicServiceImpl adminTopicService;

    private User admin;
    private Topic existingTopic;
    private TopicRequest validRequest;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        existingTopic = new Topic();
        existingTopic.setId(10L);
        existingTopic.setName("Java Programming");
        existingTopic.setDescription("Learn Java");

        validRequest = new TopicRequest();
        validRequest.setName("Spring Boot");
        validRequest.setDescription("Spring Boot basics");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("12. Tao topic thanh cong")
    void createTopic_success() {
        when(topicRepository.existsByNameIgnoreCase("Spring Boot")).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenAnswer(inv -> {
            Topic t = inv.getArgument(0);
            t.setId(20L);
            return t;
        });

        Topic result = adminTopicService.createTopic(validRequest, "admin");

        assertThat(result.getName()).isEqualTo("Spring Boot");
        verify(topicRepository).save(any(Topic.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("13. Khong tao topic trung ten (case-insensitive)")
    void createTopic_duplicateName() {
        when(topicRepository.existsByNameIgnoreCase("Spring Boot")).thenReturn(true);

        assertThatThrownBy(() -> adminTopicService.createTopic(validRequest, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã tồn tại");
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("14. Cap nhat topic thanh cong")
    void updateTopic_success() {
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(topicRepository.existsByNameIgnoreCaseAndIdNot("Spring Boot", 10L)).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenReturn(existingTopic);

        Topic result = adminTopicService.updateTopic(10L, validRequest, "admin");

        assertThat(existingTopic.getName()).isEqualTo("Spring Boot");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("15. Khong update trung ten topic khac")
    void updateTopic_duplicateNameWithOther() {
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(topicRepository.existsByNameIgnoreCaseAndIdNot("Spring Boot", 10L)).thenReturn(true);

        assertThatThrownBy(() -> adminTopicService.updateTopic(10L, validRequest, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã tồn tại");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("16. Khong xoa topic co post")
    void deleteTopic_hasPostsCannotDelete() {
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(postRepository.existsByTopicId(10L)).thenReturn(true);

        assertThatThrownBy(() -> adminTopicService.deleteTopic(10L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bài viết");
    }

    @Test
    @DisplayName("17. Xoa topic chua co post thanh cong")
    void deleteTopic_noPostsSuccess() {
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(postRepository.existsByTopicId(10L)).thenReturn(false);

        adminTopicService.deleteTopic(10L, "admin");

        verify(topicRepository).delete(existingTopic);
    }

    // ==================== AUDIT LOG ====================

    @Test
    @DisplayName("18. Create, update, delete deu ghi audit log")
    void allOperations_writeAuditLog() {
        // Create
        when(topicRepository.existsByNameIgnoreCase("Spring Boot")).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenAnswer(inv -> {
            Topic t = inv.getArgument(0);
            t.setId(20L);
            return t;
        });
        adminTopicService.createTopic(validRequest, "admin");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.CREATE_TOPIC.name());

        // Update
        reset(auditLogRepository);
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(topicRepository.existsByNameIgnoreCaseAndIdNot("Spring Boot", 10L)).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenReturn(existingTopic);
        adminTopicService.updateTopic(10L, validRequest, "admin");
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.UPDATE_TOPIC.name());

        // Delete
        reset(auditLogRepository);
        when(topicRepository.findById(10L)).thenReturn(Optional.of(existingTopic));
        when(postRepository.existsByTopicId(10L)).thenReturn(false);
        adminTopicService.deleteTopic(10L, "admin");
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.DELETE_TOPIC.name());
    }
}
