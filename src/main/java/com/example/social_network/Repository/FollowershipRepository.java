package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Followership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowershipRepository extends JpaRepository<Followership, String> {

    // Tìm record theo cặp (người gửi = userFollower, người nhận = userChecked)
    // để kiểm tra đã tồn tại lời mời/kết bạn chưa (khớp UQ id_user_checked + id_user_follower).
    Optional<Followership> findByUserFollower_IdAndUserChecked_Id(String userFollowerId, String userCheckedId);
}
