package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.PostMediaRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostShareDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostMedia;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.MediaType;
import com.example.social_network.models.Enum.PostStatus;
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

    /**
     * Tạo bài viết kèm media trong 1 transaction.
     * <p>
     * NGOẠI LỆ CONVENTION (chỉ áp dụng cho method @Transactional này): khối catch
     * KHÔNG return ResponseHelper lỗi như các Service khác — phải throw lại
     * RuntimeException sau khi log để Spring rollback toàn bộ (Post + PostMedia).
     * Nếu catch rồi return bình thường, Spring sẽ KHÔNG rollback.
     * <p>
     * Method là public và được gọi từ Controller qua Spring proxy (không self-invocation)
     * nên @Transactional có hiệu lực.
     */
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

    /**
     * Share (repost nội bộ) 1 bài viết trong 1 transaction: tạo Post share mới + tăng
     * share_count của bài được share TRỰC TIẾP.
     * <p>
     * Mỗi lượt share là 1 bài độc lập: bài share mới trỏ thẳng vào ĐÚNG bài mà người
     * dùng bấm share (id_original_post = bài đó), và share_count được cộng cho chính
     * bài đó — KHÔNG dồn về bài gốc đầu chuỗi. Nếu share lại 1 bài vốn đã là share thì
     * hình thành chuỗi (C -> B -> A), phản ánh đúng "ai lan truyền từ ai"; độ sâu hiển
     * thị card lồng do FE tự giới hạn.
     * <p>
     * Ngoại lệ: KHÔNG cho share tiếp 1 bài đã mất gốc (is_shared = true nhưng
     * originalPost đã bị xoá) — chặn lan truyền thêm bài đã hỏng gốc.
     * <p>
     * NGOẠI LỆ CONVENTION (method @Transactional): khối catch phải throw lại
     * RuntimeException sau khi log để Spring rollback đúng cả 2 thao tác ghi —
     * giống pattern insert (Post) và react (Like).
     */
    @Override
    @Transactional
    public ResponseEntity<?> share(PostShareDto dto, String userId, String ip) {
        try {
            // 1. Validate input
            if (dto.getOriginalPostId() == null || dto.getOriginalPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "originalPostId is required"));
            }
            if (dto.getText() != null && dto.getText().length() > 5000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "text exceeds maximum length of 5000 characters"));
            }

            // 2. Bài được share trực tiếp phải tồn tại
            Post target = postRepository.findById(dto.getOriginalPostId().trim()).orElse(null);
            if (target == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "Post not found with id = " + dto.getOriginalPostId()));
            }

            // 2b. Không cho share tiếp 1 bài đã mất gốc: nếu bài định share vốn là 1 lượt
            //     share (is_shared = true) nhưng bài nó từng trỏ tới đã bị xoá
            //     (originalPost = null do ON DELETE SET NULL) thì coi như đã hỏng gốc,
            //     không cho lan truyền thêm.
            if (Boolean.TRUE.equals(target.getIsShared()) && target.getOriginalPost() == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Không thể share bài viết đã mất bài gốc"));
            }

            // 3. Người share lấy từ JWT subject (userId), KHÔNG nhận từ request
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found with id = " + userId));
            }

            // 4. Tạo & lưu Post share trỏ thẳng vào bài được share trực tiếp
            //    (isPinned/counts set trong @PrePersist)
            Post sharePost = new Post();
            sharePost.setText(dto.getText() != null ? dto.getText().trim() : null);
            sharePost.setPostType(PostType.TEXT);
            sharePost.setStatus(PostStatus.PUBLISHED);
            sharePost.setOriginalPost(target);
            sharePost.setIsShared(true);
            sharePost.setUser(user);
            postRepository.save(sharePost);

            // 5. Tăng share_count của chính bài được share trực tiếp lên 1
            //    (target đang được quản lý trong transaction nên set + save trực tiếp)
            target.setShareCount((target.getShareCount() == null ? 0 : target.getShareCount()) + 1);
            postRepository.save(target);

            logger.info("User {} shared Post {} as Post {}. IP: {}",
                    userId, target.getId(), sharePost.getId(), ip);
            // 6. Thành công
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "SHARE POST SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in share post: {}", e.getMessage());
            // BẮT BUỘC throw lại để @Transactional rollback (KHÔNG return như convention thường)
            throw new RuntimeException("SHARE POST FAILED: " + e.getMessage(), e);
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

    /**
     * Xoá bài viết. Nếu bài bị xoá là 1 lượt share (is_shared = true và còn trỏ tới bài
     * được share), giảm share_count của ĐÚNG bài mà nó trỏ tới (bài được share trực tiếp)
     * đi 1 TRƯỚC khi xoá (không cho xuống dưới 0) — đối xứng với lúc tạo share.
     * <p>
     * Khi xoá 1 bài đang có người khác share nó: KHÔNG xoá các bài share đó — DB tự set
     * id_original_post = NULL nhờ ON DELETE SET NULL; is_shared của các bài share giữ
     * nguyên true để FE hiển thị "Bài viết gốc không còn tồn tại".
     * <p>
     * NGOẠI LỆ CONVENTION (method @Transactional): có thể ghi 2 thao tác (giảm share_count
     * + xoá Post) -> catch phải throw lại RuntimeException để Spring rollback đúng cả 2.
     */
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

            // TODO: kiểm tra quyền sở hữu — chỉ tác giả mới được xóa:
            // if (!post.getUser().getId().equals(userId)) return 403 FORBIDDEN;

            // Bài bị xoá là 1 lượt share -> giảm share_count của bài nó trỏ tới trực tiếp (không âm)
            if (Boolean.TRUE.equals(post.getIsShared()) && post.getOriginalPost() != null) {
                Post sharedPost = post.getOriginalPost();
                int current = sharedPost.getShareCount() == null ? 0 : sharedPost.getShareCount();
                sharedPost.setShareCount(Math.max(0, current - 1));
                postRepository.save(sharedPost);
            }

            postRepository.deleteById(dto.getId());

            logger.info("User {} deleted Post {} successfully. IP: {}", userId, dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE POST SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in delete post: {}", e.getMessage());
            // BẮT BUỘC throw lại để @Transactional rollback (KHÔNG return như convention thường)
            throw new RuntimeException("DELETE POST FAILED: " + e.getMessage(), e);
        }
    }
}
