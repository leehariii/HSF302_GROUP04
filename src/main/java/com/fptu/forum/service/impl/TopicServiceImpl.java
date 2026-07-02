package com.fptu.forum.service.impl;

import com.fptu.forum.dto.request.TopicRequest;
import com.fptu.forum.entity.Topic;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.TopicRepository;
import com.fptu.forum.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation cua TopicService.
 * Admin CRUD topic.
 */
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    @Override
    public Topic findById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", id));
    }

    @Override
    @Transactional
    public Topic create(TopicRequest request) {
        if (topicRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Ten topic '" + request.getName() + "' da ton tai.");
        }
        Topic topic = new Topic();
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        return topicRepository.save(topic);
    }

    @Override
    @Transactional
    public Topic update(Long id, TopicRequest request) {
        Topic topic = findById(id);
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        return topicRepository.save(topic);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Topic topic = findById(id);
        topicRepository.delete(topic);
    }
}
