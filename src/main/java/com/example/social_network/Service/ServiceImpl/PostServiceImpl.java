package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.PostMediaRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostMedia;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.MediaType;
import com.example.social_network.models.Enum.PostType;
import com.example.social_network.models.Enum.PostVisibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Override
    public Object getList(String id, String userId, String postId, int pageIdx, int pageSize) {
        try {
            List<Post> results;
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Post> postsPage;
            if (userId == null && postId == null && id == null) {
                postsPage = postRepository.findAll(paging);
            } else {
                postsPage = postRepository.findPosts(id, userId != null ? userId.trim() : null,
                        postId != null ? postId.trim() : null, paging);
            }
            results = postsPage.getContent();
            logger.info("get list post success page: {} size: {}", pageIdx, pageSize);
            return ResponseHelper.getResponses(results, postsPage.getTotalElements(), postsPage.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getList post: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> insert(PostInsertDto dto, String userId, String ip) {
        try {
            List<PostInsertDto.MediaItem> media = dto.getMedia();
            boolean mediaEmpty = (media == null || media.isEmpty());

            // 1. Validate: media rỗng thì text bắt buộc
            if (mediaEmpty && (dto.getText() == null || dto.getText().trim().isEmpty())) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "text is required when media is empty"));
            }
            if (dto.getText() != null && dto.getText().length() > 5000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "text exceeds maximum length of 5000 characters"));
            }
            // visibility bắt buộc và phải hợp lệ
            if (dto.getVisibility() == null || dto.getVisibility().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "visibility is required"));
            }
            PostVisibility visibility;
            try {
                visibility = PostVisibility.valueOf(dto.getVisibility().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "visibility không hợp lệ: " + dto.getVisibility()));
            }

            // Tác giả lấy từ JWT subject (userId), KHÔNG nhận từ request
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + userId));
            }

            // 2. Suy ra post_type từ số lượng/loại media
            PostType postType;
            if (mediaEmpty) {
                postType = PostType.TEXT;
            } else if (media.size() == 1) {
                postType = "VIDEO".equalsIgnoreCase(media.get(0).getType()) ? PostType.VIDEO : PostType.IMAGE;
            } else {
                postType = PostType.CAROUSEL;
            }

            // 4. Tạo Post, lưu trước để có id_post (status/isPinned/counts set trong @PrePersist)
            Post post = new Post();
            post.setText(dto.getText() != null ? dto.getText().trim() : null);
            post.setPostType(postType);
            post.setVisibility(visibility);
            post.setUser(user);
            postRepository.save(post);

            // 5-6. Tạo & lưu PostMedia — lấy nguyên field từ mảng media (KHÔNG gọi lại Cloudinary)
            if (!mediaEmpty) {
                List<PostMedia> mediaEntities = new ArrayList<>();
                for (PostInsertDto.MediaItem item : media) {
                    PostMedia pm = new PostMedia();
                    pm.setPost(post);
                    pm.setMediaUrl(item.getUrl());
                    pm.setMediaType("VIDEO".equalsIgnoreCase(item.getType()) ? MediaType.VIDEO : MediaType.IMAGE);
                    pm.setDisplayOrder(item.getOrder());
                    pm.setWidth(item.getWidth());
                    pm.setHeight(item.getHeight());
                    pm.setDurationSec(item.getDurationSec());
                    mediaEntities.add(pm);
                }
                postMediaRepository.saveAll(mediaEntities);
            }

            logger.info("User {} created Post {} (type {}) with {} media. IP: {}",
                    userId, post.getId(), postType, mediaEmpty ? 0 : media.size(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "INSERT POST SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in insert post: {}", e.getMessage());
            throw new RuntimeException("INSERT POST FAILED: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> update(PostUpdateDto dto, String userId, String ip) {
        try {
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }

            Post post = postRepository.findById(dto.getId()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found with id = " + dto.getId()));
            }

            post.setText(dto.getText());
            if (dto.getIsPinned() != null) {
                post.setIsPinned(dto.getIsPinned());
            }

            // visibility: chỉ cập nhật khi có gửi; validate như lúc tạo bài
            if (dto.getVisibility() != null && !dto.getVisibility().trim().isEmpty()) {
                PostVisibility visibility;
                try {
                    visibility = PostVisibility.valueOf(dto.getVisibility().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "visibility không hợp lệ: " + dto.getVisibility()));
                }
                post.setVisibility(visibility);
            }
            
            postRepository.save(post);

            logger.info("User {} updated Post {} successfully. IP: {}", userId, post.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "UPDATE POST SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in update post: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> delete(DeleteDto dto, String userId, String ip) {
        try {
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }

            Post post = postRepository.findById(dto.getId()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found with id = " + dto.getId()));
            }

            postRepository.deleteById(dto.getId());
            logger.info("User {} deleted Post {} successfully. IP: {}", userId, dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE POST SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in delete post: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
