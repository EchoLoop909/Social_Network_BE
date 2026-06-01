package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Like;
import com.example.social_network.models.Enum.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {

    Optional<Like> findByTargetTypeAndTargetIdAndUser_Id(LikeTargetType targetType, String targetId, String userId);
}