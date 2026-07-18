package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.AuditLogRepository;
import com.fptu.forum.repository.PostRepository;
import com.fptu.forum.repository.TopicRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.AdminTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation cua AdminTopicService.
 * Moi thao tac tao/sua/xoa deu ghi audit log trong cung @Transactional.
 */
@Service
@RequiredArgsConstructor
public class AdminTopicServiceImpl implements AdminTopicService {

    private final TopicRepository topicRepository;
    private final PostRepository  postRepository;
    private final UserRepository  userRepository;
    private final AuditLogRepository auditLogRepository;

    // ---- Helper ----

    private User loadAdmin(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user: " + username));
    }

    private void saveAuditLog(User actor, AuditAction action, String note) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action.name());
        log.setNote(note);
        auditLogRepository.save(log);
    }

    // ---- Read ----

    @Override
    @Transactional(readOnly = true)
    public Page<Topic> getTopics(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return topicRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        return topicRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic getTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", id));
    }

    // ---- Create ----

    @Override
    @Transactional
    public Topic createTopic(TopicRequest request, String currentUsername) {
        String name = request.getName().trim();
        String desc = request.getDescription() != null ? request.getDescription().trim() : null;

        if (topicRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Tên chủ đề '" + name + "' đã tồn tại.");
        }

        Topic topic = new Topic();
        topic.setName(name);
        topic.setDescription(desc);
        topicRepository.save(topic);

        User admin = loadAdmin(currentUsername);
        saveAuditLog(admin, AuditAction.CREATE_TOPIC,
                "Created topic: " + name + " (ID: " + topic.getId() + ")");

        return topic;
    }

    // ---- Update ----

    @Override
    @Transactional
    public Topic updateTopic(Long id, TopicRequest request, String currentUsername) {
        Topic topic = getTopicById(id);
        String name = request.getName().trim();
        String desc = request.getDescription() != null ? request.getDescription().trim() : null;

        // Kiem tra trung ten voi topic KHAC (khong bao loi khi ten khong doi)
        if (topicRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException("Tên chủ đề '" + name + "' đã tồn tại.");
        }

        topic.setName(name);
        topic.setDescription(desc);
        topicRepository.save(topic);

        User admin = loadAdmin(currentUsername);
        saveAuditLog(admin, AuditAction.UPDATE_TOPIC,
                "Updated topic: " + name + " (ID: " + id + ")");

        return topic;
    }

    // ---- Delete ----

    @Override
    @Transactional
    public void deleteTopic(Long id, String currentUsername) {
        Topic topic = getTopicById(id);

        if (postRepository.existsByTopicId(id)) {
            throw new BusinessException(
                    "Không thể xóa chủ đề vì đang có bài viết sử dụng.");
        }

        String topicName = topic.getName();
        User admin = loadAdmin(currentUsername);

        // Ghi audit log truoc khi xoa (de con reference)
        saveAuditLog(admin, AuditAction.DELETE_TOPIC,
                "Deleted topic: " + topicName + " (ID: " + id + ")");

        topicRepository.delete(topic);
    }
}
