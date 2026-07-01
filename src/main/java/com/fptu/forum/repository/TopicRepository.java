package com.fptu.forum.repository;

import com.fptu.forum.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository cho entity Topic.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Kiem tra ten topic da ton tai chua (Admin CRUD)
    boolean existsByName(String name);

    // Tim theo ten (ignore case)
    List<Topic> findByNameContainingIgnoreCase(String keyword);
}
