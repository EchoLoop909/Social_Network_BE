package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.BookmarkRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.BookmarkService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Bookmark;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkServiceImpl.class);

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public ResponseEntity<?> toggle(String postId, String userId, String ip) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }
            Post post = postRepository.findById(postId.trim()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found with id = " + postId));
            }

            Optional<Bookmark> existing = bookmarkRepository.findByUser_IdAndPost_Id(userId, postId.trim());
            boolean saved;
            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                saved = false;
                logger.info("User {} un-saved post {}. IP: {}", userId, postId, ip);
            } else {
                Bookmark b = new Bookmark();
                b.setUser(user);
                b.setPost(post);
                bookmarkRepository.save(b);
                saved = true;
                logger.info("User {} saved post {}. IP: {}", userId, postId, ip);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("saved", saved);
            return ResponseHelper.buildResponse(data, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in toggle bookmark: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getMyBookmarks(String userId, int pageIdx, int pageSize) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Bookmark> page = bookmarkRepository.findByUserOrderByNewest(userId, paging);
            List<Post> posts = page.getContent().stream().map(Bookmark::getPost).collect(Collectors.toList());

            logger.info("getMyBookmarks of {} -> {} bài", userId, posts.size());
            return ResponseHelper.getResponses(posts, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getMyBookmarks: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getMyBookmarkPostIds(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            List<String> ids = bookmarkRepository.findPostIds(userId);
            logger.info("getMyBookmarkPostIds of {} -> {} id", userId, ids.size());
            return ResponseHelper.getResponses(ids, ids.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getMyBookmarkPostIds: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }
}
