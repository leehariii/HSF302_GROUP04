package com.fptu.forum.service;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service rieng danh cho Admin quan ly Topic (CRUD + audit log).
 * Tach khoi TopicService cong khai (lay danh sach cho forum).
 */
public interface AdminTopicService {

    Page<Topic> getTopics(String keyword, Pageable pageable);

    Topic getTopicById(Long id);

    /**
     * Tao topic moi. Ghi audit log trong cung transaction.
     */
    Topic createTopic(TopicRequest request, String currentUsername);

    /**
     * Cap nhat topic. Ghi audit log trong cung transaction.
     */
    Topic updateTopic(Long id, TopicRequest request, String currentUsername);

    /**
     * Xoa topic (chi khi chua co post tham chieu).
     * Ghi audit log trong cung transaction.
     */
    void deleteTopic(Long id, String currentUsername);
}
