package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, String> {

    Optional<Hashtag> findByName(String name);

    // Tìm hashtag theo từ khóa, xu hướng (post_count) giảm dần
    Page<Hashtag> findByNameContainingIgnoreCaseOrderByPostCountDesc(String name, Pageable pageable);
}
