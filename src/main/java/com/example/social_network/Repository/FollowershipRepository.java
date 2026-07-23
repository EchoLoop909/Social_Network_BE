package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Followership;
import com.example.social_network.models.Enum.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowershipRepository extends JpaRepository<Followership, String> {

    // Tìm record theo cặp (người gửi = userFollower, người nhận = userChecked)
    // để kiểm tra đã tồn tại lời mời/kết bạn chưa (khớp UQ id_user_checked + id_user_follower).
    Optional<Followership> findByUserFollower_IdAndUserChecked_Id(String userFollowerId, String userCheckedId);

    // Kiểm tra người xem (follower) có quan hệ ở 1 trạng thái cụ thể với chủ tài khoản (checked) không.
    // Dùng để mở quyền xem tài khoản riêng tư (chỉ khi status = ACCEPTED).
    boolean existsByUserFollower_IdAndUserChecked_IdAndStatus(String userFollowerId, String userCheckedId, FollowStatus status);

    // Hai user CÓ phải bạn bè (ACCEPTED) không — xét CẢ 2 CHIỀU (ai gửi lời mời trước cũng được).
    @Query("SELECT COUNT(f) > 0 FROM Followership f WHERE f.status = :accepted " +
            "AND ((f.userFollower.id = :a AND f.userChecked.id = :b) " +
            "  OR (f.userFollower.id = :b AND f.userChecked.id = :a))")
    boolean existsAcceptedBetween(@Param("a") String a, @Param("b") String b, @Param("accepted") FollowStatus accepted);

    // Xóa MỌI bản ghi quan hệ giữa 2 user theo cả 2 chiều (A->B và B->A) — dùng cho unfriend.
    @Modifying
    @Query("DELETE FROM Followership f WHERE (f.userFollower.id = :a AND f.userChecked.id = :b) " +
            "OR (f.userFollower.id = :b AND f.userChecked.id = :a)")
    int deleteRelationBetween(@Param("a") String a, @Param("b") String b);

    // Lời mời ĐẾN mình: mình là người được request (userChecked), status thuộc danh sách (FOLLOWING/PENDING).
    @Query("SELECT f FROM Followership f JOIN FETCH f.userFollower " +
            "WHERE f.userChecked.id = :me AND f.status IN :statuses ORDER BY f.createTime DESC")
    List<Followership> findIncomingRequests(@Param("me") String me,
                                            @Param("statuses") Collection<FollowStatus> statuses);

    // Lời mời mình ĐÃ GỬI ĐI: mình là người gửi (userFollower), status thuộc danh sách (FOLLOWING/PENDING).
    @Query("SELECT f FROM Followership f JOIN FETCH f.userChecked " +
            "WHERE f.userFollower.id = :me AND f.status IN :statuses ORDER BY f.createTime DESC")
    List<Followership> findOutgoingRequests(@Param("me") String me,
                                            @Param("statuses") Collection<FollowStatus> statuses);

    // Bạn bè: quan hệ ACCEPTED, mình ở 1 trong 2 phía (follower hoặc checked).
    @Query("SELECT f FROM Followership f JOIN FETCH f.userFollower JOIN FETCH f.userChecked " +
            "WHERE f.status = :accepted AND (f.userFollower.id = :me OR f.userChecked.id = :me) " +
            "ORDER BY f.acceptedAt DESC")
    List<Followership> findFriends(@Param("me") String me, @Param("accepted") FollowStatus accepted);

    // Id của tất cả user đã có quan hệ với mình (bất kể chiều/status) — để loại khỏi gợi ý.
    @Query("SELECT CASE WHEN f.userFollower.id = :me THEN f.userChecked.id ELSE f.userFollower.id END " +
            "FROM Followership f WHERE f.userFollower.id = :me OR f.userChecked.id = :me")
    List<String> findRelatedUserIds(@Param("me") String me);

    // Id các tác giả mà mình đang theo dõi / là bạn (status thuộc danh sách, xét CẢ 2 CHIỀU) —
    // dùng để ƯU TIÊN bài của họ lên đầu trong gợi ý (recommend).
    @Query("SELECT CASE WHEN f.userFollower.id = :me THEN f.userChecked.id ELSE f.userFollower.id END " +
            "FROM Followership f WHERE (f.userFollower.id = :me OR f.userChecked.id = :me) " +
            "AND f.status IN :statuses")
    List<String> findFollowedOrFriendIds(@Param("me") String me,
                                         @Param("statuses") Collection<FollowStatus> statuses);

    // Giữa 2 user CÓ đang bị chặn không (xét CẢ 2 CHIỀU) — dùng để ẩn nhau.
    @Query("SELECT COUNT(f) > 0 FROM Followership f WHERE f.status = :blocked " +
            "AND ((f.userFollower.id = :a AND f.userChecked.id = :b) " +
            "  OR (f.userFollower.id = :b AND f.userChecked.id = :a))")
    boolean existsBlockBetween(@Param("a") String a, @Param("b") String b, @Param("blocked") FollowStatus blocked);

    // Id các user đang có quan hệ BLOCKED với mình (mình chặn họ HOẶC họ chặn mình).
    @Query("SELECT CASE WHEN f.userFollower.id = :me THEN f.userChecked.id ELSE f.userFollower.id END " +
            "FROM Followership f WHERE f.status = :blocked AND (f.userFollower.id = :me OR f.userChecked.id = :me)")
    List<String> findBlockedRelatedIds(@Param("me") String me, @Param("blocked") FollowStatus blocked);

    // Danh sách user mà MÌNH đã chặn (userFollower = me, status = BLOCKED).
    @Query("SELECT f FROM Followership f JOIN FETCH f.userChecked " +
            "WHERE f.userFollower.id = :me AND f.status = :blocked ORDER BY f.createTime DESC")
    List<Followership> findBlockedUsers(@Param("me") String me, @Param("blocked") FollowStatus blocked);
}
