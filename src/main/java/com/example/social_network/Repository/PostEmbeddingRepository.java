package com.example.social_network.Repository;

import com.example.social_network.models.Entity.PostEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, String> {
    // JpaRepository đã có: save, findById, existsById, count, findAll
}
