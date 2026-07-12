package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import com.example.social_network.Repository.FollowershipRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.FollowershipService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Followership;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.FollowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FollowershipServiceImpl implements FollowershipService {
    private static final Logger logger = LoggerFactory.getLogger(FollowershipServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowershipRepository followershipRepository;

    @Override
    public ResponseEntity<?> sendFriendRequest(FriendRequestDto dto, String userId, String ip) {
        try {
            // 1. Validate input
            if (dto.getTargetUserId() == null || dto.getTargetUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "targetUserId is required"));
            }
            String targetId = dto.getTargetUserId().trim();

            // 2. Không cho tự kết bạn với chính mình (A = userId từ JWT)
            if (userId != null && userId.equals(targetId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Không thể tự kết bạn với chính mình"));
            }

            // Người gửi A phải tồn tại
            User follower = userRepository.findById(userId).orElse(null);
            if (follower == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + userId));
            }

            // 3. Người nhận B phải tồn tại
            User checked = userRepository.findById(targetId).orElse(null);
            if (checked == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + targetId));
            }

            // 4. Kiểm tra record đã tồn tại theo cặp (userFollower=A, userChecked=B)
            Followership existing = followershipRepository
                    .findByUserFollower_IdAndUserChecked_Id(userId, targetId).orElse(null);
            if (existing != null) {
                if (existing.getStatus() == FollowStatus.PENDING) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "Đã gửi lời mời trước đó"));
                }
                if (existing.getStatus() == FollowStatus.ACCEPTED) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "Hai người đã là bạn bè"));
                }
                // REJECTED -> cho gửi lại: tái sử dụng chính record cũ, đổi về PENDING
                // (tránh tạo record mới vi phạm UQ id_user_checked + id_user_follower)
                existing.setStatus(FollowStatus.PENDING);
                existing.setUpdateTime(LocalDateTime.now());
                followershipRepository.save(existing);

                logger.info("User {} re-sent friend request to {}. IP: {}", userId, targetId, ip);
                return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                        new ResponseMess(0, "SEND FRIEND REQUEST SUCCESS"));
            }

            // 5. Chưa có record -> tạo mới, set PENDING TƯỜNG MINH
            //    (vì @PrePersist của Followership đang default status = ACCEPTED)
            Followership followership = new Followership();
            followership.setUserFollower(follower);
            followership.setUserChecked(checked);
            followership.setStatus(FollowStatus.PENDING);
            followershipRepository.save(followership);

            logger.info("User {} sent friend request to {}. IP: {}", userId, targetId, ip);
            // 6. Thành công
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "SEND FRIEND REQUEST SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in sendFriendRequest: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> acceptFriendRequest(AcceptFriendRequestDto dto, String userId, String ip) {
        try {
            // 1. Validate input
            if (dto.getRequesterId() == null || dto.getRequesterId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "requesterId is required"));
            }
            String requesterId = dto.getRequesterId().trim();

            // 2. Tìm record theo cặp (userFollower = B (requester), userChecked = A (người đăng nhập)).
            //    Đặt userId (JWT) làm userChecked -> query chỉ khớp khi A ĐÚNG là người NHẬN,
            //    nên B (người gửi) tự gọi accept sẽ không tìm thấy record (không thể tự duyệt).
            Followership followership = followershipRepository
                    .findByUserFollower_IdAndUserChecked_Id(requesterId, userId).orElse(null);
            if (followership == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "Không tìm thấy lời mời kết bạn để chấp nhận"));
            }

            // 3. Kiểm tra trạng thái
            if (followership.getStatus() == FollowStatus.ACCEPTED) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Hai người đã là bạn bè"));
            }
            if (followership.getStatus() != FollowStatus.PENDING) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Lời mời không ở trạng thái chờ duyệt"));
            }

            // 4. Duyệt: PENDING -> ACCEPTED, set acceptedAt + updateTime
            LocalDateTime now = LocalDateTime.now();
            followership.setStatus(FollowStatus.ACCEPTED);
            followership.setAcceptedAt(now);
            followership.setUpdateTime(now);
            followershipRepository.save(followership);

            logger.info("User {} accepted friend request from {}. IP: {}", userId, requesterId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "ACCEPT FRIEND REQUEST SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in acceptFriendRequest: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }
}
