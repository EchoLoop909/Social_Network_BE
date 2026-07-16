package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.AcceptFriendRequestDto;
import com.example.social_network.Payload.Request.BlockUserDto;
import com.example.social_network.Payload.Request.FriendRequestDto;
import com.example.social_network.Payload.Request.UnfriendDto;
import com.example.social_network.Repository.FollowershipRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.FollowershipService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Dto.UserSummaryDto;
import com.example.social_network.models.Entity.Followership;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.FollowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            // 3b. Không cho gửi lời mời nếu đang có quan hệ BLOCKED giữa 2 người (bất kể ai chặn ai)
            if (followershipRepository.existsBlockBetween(userId, targetId, FollowStatus.BLOCKED)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN,
                        new ResponseMess(1, "Không thể gửi lời mời do đã chặn hoặc bị chặn"));
            }

            // 4. Kiểm tra record đã tồn tại theo cặp (userFollower=A, userChecked=B)
            Followership existing = followershipRepository
                    .findByUserFollower_IdAndUserChecked_Id(userId, targetId).orElse(null);
            if (existing != null) {
                if (existing.getStatus() == FollowStatus.FOLLOWING) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "Bạn đang theo dõi người này rồi"));
                }
                if (existing.getStatus() == FollowStatus.ACCEPTED) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "Hai người đã là bạn bè"));
                }
                // PENDING/REJECTED cũ -> chuyển sang FOLLOWING
                existing.setStatus(FollowStatus.FOLLOWING);
                existing.setUpdateTime(LocalDateTime.now());
                followershipRepository.save(existing);

                logger.info("User {} re-followed {}. IP: {}", userId, targetId, ip);
                return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                        new ResponseMess(0, "SEND FRIEND REQUEST SUCCESS"));
            }

            // 5. Chưa có record -> tạo mới, set FOLLOWING (đang theo dõi) TƯỜNG MINH.
            Followership followership = new Followership();
            followership.setUserFollower(follower);
            followership.setUserChecked(checked);
            followership.setStatus(FollowStatus.FOLLOWING);
            followershipRepository.save(followership);

            logger.info("User {} sent friend request to {}. IP: {}", userId, targetId, ip);
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
            // Chỉ chấp nhận được khi đang ở FOLLOWING (đang theo dõi) hoặc PENDING (dữ liệu cũ)
            if (followership.getStatus() != FollowStatus.FOLLOWING
                    && followership.getStatus() != FollowStatus.PENDING) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Lời mời không ở trạng thái có thể chấp nhận"));
            }

            // 4. Duyệt: FOLLOWING/PENDING -> ACCEPTED, set acceptedAt + updateTime
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

    @Override
    @Transactional
    public ResponseEntity<?> unfriend(UnfriendDto dto, String userId, String ip) {
        try {
            // 1. Validate input
            if (dto.getTargetUserId() == null || dto.getTargetUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "targetUserId is required"));
            }
            String targetId = dto.getTargetUserId().trim();

            // 2. Không cho tự hủy kết bạn với chính mình
            if (userId != null && userId.equals(targetId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Không thể tự hủy kết bạn với chính mình"));
            }

            // 3. Xóa mọi quan hệ giữa A (userId) và B (targetId) theo CẢ 2 CHIỀU
            int deleted = followershipRepository.deleteRelationBetween(userId, targetId);
            if (deleted == 0) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "Hai người chưa có quan hệ theo dõi/kết bạn để hủy"));
            }

            logger.info("User {} unfriended {} (deleted {} record(s)). IP: {}", userId, targetId, deleted, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "UNFRIEND SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in unfriend: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getFriends(String userId) {
        try {
            List<Followership> rels = followershipRepository.findFriends(userId, FollowStatus.ACCEPTED);
            // Lấy "người còn lại" trong mỗi cặp (không phải chính mình)
            List<UserSummaryDto> result = rels.stream()
                    .map(f -> f.getUserFollower().getId().equals(userId)
                            ? f.getUserChecked() : f.getUserFollower())
                    .map(UserSummaryDto::from)
                    .collect(Collectors.toList());
            logger.info("getFriends of {} -> {} bạn", userId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getFriends: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getFriendRequests(String userId) {
        try {
            List<Followership> reqs = followershipRepository.findIncomingRequests(
                    userId, Arrays.asList(FollowStatus.FOLLOWING, FollowStatus.PENDING));
            List<UserSummaryDto> result = reqs.stream()
                    .map(f -> UserSummaryDto.from(f.getUserFollower()))
                    .collect(Collectors.toList());
            logger.info("getFriendRequests of {} -> {} lời mời", userId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getFriendRequests: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getSentRequests(String userId) {
        try {
            List<Followership> reqs = followershipRepository.findOutgoingRequests(
                    userId, Arrays.asList(FollowStatus.FOLLOWING, FollowStatus.PENDING));
            List<UserSummaryDto> result = reqs.stream()
                    .map(f -> UserSummaryDto.from(f.getUserChecked()))
                    .collect(Collectors.toList());
            logger.info("getSentRequests of {} -> {} lời mời đã gửi", userId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getSentRequests: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getSuggestions(String userId) {
        try {
            List<String> related = followershipRepository.findRelatedUserIds(userId);
            Set<String> exclude = new HashSet<>(related);
            exclude.add(userId);
            List<UserSummaryDto> result = userRepository.findAll().stream()
                    .filter(u -> !exclude.contains(u.getId()))
                    .map(UserSummaryDto::from)
                    .collect(Collectors.toList());
            logger.info("getSuggestions of {} -> {} gợi ý", userId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getSuggestions: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> rejectFriendRequest(AcceptFriendRequestDto dto, String userId, String ip) {
        try {
            // 1. Validate input
            if (dto.getRequesterId() == null || dto.getRequesterId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "requesterId is required"));
            }
            String requesterId = dto.getRequesterId().trim();

            // 2. Tìm bản ghi lời mời (requester -> mình)
            Followership followership = followershipRepository
                    .findByUserFollower_IdAndUserChecked_Id(requesterId, userId).orElse(null);
            if (followership == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "Không tìm thấy lời mời để từ chối"));
            }
            if (followership.getStatus() == FollowStatus.ACCEPTED) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Hai người đã là bạn bè, không thể từ chối"));
            }

            // 3. Từ chối = xóa bản ghi khỏi danh sách chờ
            followershipRepository.delete(followership);

            logger.info("User {} rejected friend request from {}. IP: {}", userId, requesterId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "REJECT FRIEND REQUEST SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in rejectFriendRequest: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> blockUser(BlockUserDto dto, String userId, String ip) {
        try {
            // 1. Validate
            if (dto.getTargetUserId() == null || dto.getTargetUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "targetUserId is required"));
            }
            String targetId = dto.getTargetUserId().trim();
            if (userId != null && userId.equals(targetId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Không thể tự chặn chính mình"));
            }
            User me = userRepository.findById(userId).orElse(null);
            if (me == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + userId));
            }
            User target = userRepository.findById(targetId).orElse(null);
            if (target == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + targetId));
            }

            // 2. Nếu đã chặn rồi thì thôi
            if (followershipRepository.existsBlockBetween(userId, targetId, FollowStatus.BLOCKED)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Đã tồn tại quan hệ chặn giữa hai người"));
            }

            // 3. Xóa mọi quan hệ cũ (2 chiều) rồi tạo bản ghi BLOCKED: me -> target
            followershipRepository.deleteRelationBetween(userId, targetId);
            Followership block = new Followership();
            block.setUserFollower(me);      // người CHẶN
            block.setUserChecked(target);   // người BỊ CHẶN
            block.setStatus(FollowStatus.BLOCKED);
            followershipRepository.save(block);

            logger.info("User {} blocked {}. IP: {}", userId, targetId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "BLOCK USER SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in blockUser: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> unblockUser(BlockUserDto dto, String userId, String ip) {
        try {
            if (dto.getTargetUserId() == null || dto.getTargetUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "targetUserId is required"));
            }
            String targetId = dto.getTargetUserId().trim();

            // Chỉ người ĐÃ chặn mới bỏ chặn được: tìm bản ghi me -> target, status BLOCKED
            Followership block = followershipRepository
                    .findByUserFollower_IdAndUserChecked_Id(userId, targetId).orElse(null);
            if (block == null || block.getStatus() != FollowStatus.BLOCKED) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "Bạn chưa chặn người dùng này"));
            }
            followershipRepository.delete(block);

            logger.info("User {} unblocked {}. IP: {}", userId, targetId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "UNBLOCK USER SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in unblockUser: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getBlockedUsers(String userId) {
        try {
            List<Followership> blocks = followershipRepository.findBlockedUsers(userId, FollowStatus.BLOCKED);
            List<UserSummaryDto> result = blocks.stream()
                    .map(f -> UserSummaryDto.from(f.getUserChecked()))
                    .collect(Collectors.toList());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getBlockedUsers: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }
}
