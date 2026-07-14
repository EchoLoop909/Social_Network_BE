package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Followership;
import com.example.social_network.models.Enum.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowershipRepository extends JpaRepository<Followership, String> {

    // Tìm record theo cặp (người gửi = userFollower, người nhận = userChecked)
    // để kiểm tra đã tồn tại lời mời/kết bạn chưa (khớp UQ id_user_checked + id_user_follower).
    Optional<Followership> findByUserFollower_IdAndUserChecked_Id(String userFollowerId, String userCheckedId);

    // Kiểm tra người xem (follower) có quan hệ ở 1 trạng thái cụ thể với chủ tài khoản (checked) không.
    // Dùng để mở quyền xem tài khoản riêng tư (chỉ khi status = ACCEPTED).
    boolean existsByUserFollower_IdAndUserChecked_IdAndStatus(String userFollowerId, String userCheckedId, FollowStatus status);

    // Xóa MỌI bản ghi quan hệ giữa 2 user theo cả 2 chiều (A->B và B->A) — dùng cho unfriend.
    @Modifying
    @Query("DELETE FROM Followership f WHERE (f.userFollower.id = :a AND f.userChecked.id = :b) " +
            "OR (f.userFollower.id = :b AND f.userChecked.id = :a)")
    int deleteRelationBetween(@Param("a") String a, @Param("b") String b);
}
