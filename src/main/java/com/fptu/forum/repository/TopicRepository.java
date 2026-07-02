package com.fptu.forum.repository;

import com.fptu.forum.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho entity Topic.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Kiem tra ten topic da ton tai chua (Admin CRUD) - khong phan biet hoa thuong
    boolean existsByNameIgnoreCase(String name);

    // Kiem tra ten topic ton tai va khac topic dang sua (de tranh bao loi trung chinh no)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Tim theo ten (ignore case) - phan trang
    Page<Topic> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Tim theo ten (ignore case) - khong phan trang (dung trong dropdown chon topic)
    java.util.List<Topic> findByNameContainingIgnoreCase(String keyword);

    // Lay tat ca topic phan trang
    Page<Topic> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
