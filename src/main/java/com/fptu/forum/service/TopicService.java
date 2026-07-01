package com.fptu.forum.service;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.Topic;

import java.util.List;

/**
 * Interface cho TopicService.
 * Implementation: TopicServiceImpl.
 */
public interface TopicService {

    List<Topic> findAll();

    Topic findById(Long id);

    Topic create(TopicRequest request);

    Topic update(Long id, TopicRequest request);

    void delete(Long id);
}
