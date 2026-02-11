package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Response.Message;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Payload.Util.Status;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import com.example.social_network.models.Dto.ResponseMess;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public Object getList(String id,String userId, String postId,int pageIdx, int pageSize) {
        try{
                    List<Post> results = new ArrayList<>();
                    Pageable paging = PageRequest.of(pageIdx, pageSize);
                    Page<Post> postsPage;
                    if(userId == null && postId == null && id == null){
                        postsPage = postRepository.findAll(paging);
                        results = postsPage.getContent();
                    }else{
                        postsPage= postRepository.findPosts(id, userId != null ? userId.trim() : null,postId != null ? postId.trim() : null, paging);
                        results = postsPage.getContent();
                    }
                    logger.info("get list jobtitle success page: " + pageIdx + " size: " + pageSize);
                    return ResponseHelper.getResponses(results, postsPage.getTotalElements(), postsPage.getTotalPages(), HttpStatus.OK);
                } catch (Exception e) {
                    logger.error("error: " + e.getMessage());
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse(Status.EXCEPTION, Message.EXCEPTION, false, true));
                }
    }

    @Override
    public ResponseEntity<?> insert(PostInsertDto dto, String ip) {
        try {
            // ✅ Validate userId
            if (dto.getUserId() == null || dto.getUserId().isEmpty()) {
                return ResponseEntity.badRequest().body("userId is required");
            }

            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id = " + dto.getUserId());
            }

            // ✅ Map tay
            Post post = new Post();
            post.setText(dto.getText());
            post.setPhoto(dto.getPhoto());
            post.setUser(user);
            // id, createTime, isPinned sẽ auto set ở @PrePersist

            postRepository.save(post);

            return ResponseEntity.ok("INSERT POST SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("INSERT POST FAILED: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> update(PostUpdateDto dto, String ip) {
        try {
            // ✅ Validate postId
            if (dto.getId() == null || dto.getId().isEmpty()) {
                return ResponseEntity.badRequest().body("postId is required");
            }

            Post post = postRepository.findById(dto.getId()).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found with id = " + dto.getId());
            }

            // ✅ Update tay từng field
            post.setText(dto.getText());
            post.setPhoto(dto.getPhoto());
            if (dto.getIsPinned() != null) {
                post.setIsPinned(dto.getIsPinned());
            }

            postRepository.save(post);

            return ResponseEntity.ok("UPDATE POST SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("UPDATE POST FAILED: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> delete(DeleteDto dto, String ip) {
        try {
            if (dto.getId() == null || dto.getId().isEmpty()) {
                return ResponseEntity.badRequest().body("postId is required");
            }

            boolean exists = postRepository.existsById(dto.getId());
            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found with id = " + dto.getId());
            }

            postRepository.deleteById(dto.getId());

            return ResponseEntity.ok("DELETE POST SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("DELETE POST FAILED: " + e.getMessage());
        }
    }
}
