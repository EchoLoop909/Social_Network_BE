package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.LikeRequestDto;
import com.example.social_network.Repository.LikeRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.LikeService;
import com.example.social_network.models.Entity.Like;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {
    private static final Logger logger = LoggerFactory.getLogger(LikeServiceImpl.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public ResponseEntity<?> toggleLike(LikeRequestDto dto, String ip) {
        try {
            // 1. Validate dữ liệu đầu vào
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            if (dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("postId is required");
            }

            // 2. Kiểm tra User có tồn tại không
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id = " + dto.getUserId());
            }

            // 3. Kiểm tra Post có tồn tại không
            Post post = postRepository.findById(dto.getPostId()).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found with id = " + dto.getPostId());
            }

            // 4. Kiểm tra xem User đã like Post này chưa
            Optional<Like> existingLike = likeRepository.findByPost_IdAndUser_Id(post.getId(), user.getId());

            if (existingLike.isPresent()) {
                // NẾU ĐÃ CÓ RỒI -> XÓA ĐI (UNLIKE)
                likeRepository.delete(existingLike.get());
                logger.info("User {} unliked Post {} successfully. IP: {}", user.getId(), post.getId(), ip);
                return ResponseEntity.ok("UNLIKE POST SUCCESS");
            } else {
                // NẾU CHƯA CÓ -> THÊM MỚI VÀO (LIKE)
                Like newLike = new Like();
                newLike.setUser(user);
                newLike.setPost(post);
                // id và createTime sẽ được auto set thông qua @PrePersist trong Entity Like

                likeRepository.save(newLike);
                logger.info("User {} liked Post {} successfully. IP: {}", user.getId(), post.getId(), ip);
                return ResponseEntity.ok("LIKE POST SUCCESS");
            }

        } catch (Exception e) {
            logger.error("Error in toggleLike: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SYSTEM ERROR: " + e.getMessage());
        }
    }
}