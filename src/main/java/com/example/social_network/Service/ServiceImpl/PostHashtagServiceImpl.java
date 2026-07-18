package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.PostHashtagRequest;
import com.example.social_network.Repository.HashtagRepository;
import com.example.social_network.Repository.PostHashtagRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostHashtagService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Hashtag;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostHashtag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostHashtagServiceImpl implements PostHashtagService {
    private static final Logger logger = LoggerFactory.getLogger(PostHashtagServiceImpl.class);

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Override
    @Transactional
    public ResponseEntity<?> attach(PostHashtagRequest dto, String ip) {
        try {
            if (dto == null || dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            if (dto.getHashtagId() == null || dto.getHashtagId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "hashtagId is required"));
            }
            Post post = postRepository.findById(dto.getPostId().trim()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found"));
            }
            Hashtag hashtag = hashtagRepository.findById(dto.getHashtagId().trim()).orElse(null);
            if (hashtag == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Hashtag not found"));
            }

            PostHashtag.PostHashtagId id = new PostHashtag.PostHashtagId(post.getId(), hashtag.getId());
            if (!postHashtagRepository.existsById(id)) {
                postHashtagRepository.save(new PostHashtag(id, post, hashtag));
                hashtag.setPostCount((hashtag.getPostCount() == null ? 0 : hashtag.getPostCount()) + 1);
                hashtagRepository.save(hashtag);
            }

            logger.info("Attach hashtag {} to post {}. IP: {}", hashtag.getId(), post.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "ATTACH HASHTAG SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in attach post-hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getByPost(String postId) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            List<Hashtag> result = postHashtagRepository.findByPost_Id(postId.trim())
                    .stream().map(PostHashtag::getHashtag).collect(Collectors.toList());
            logger.info("getByPost hashtags of {} -> {}", postId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getByPost post-hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> detach(PostHashtagRequest dto, String ip) {
        try {
            if (dto == null || dto.getPostId() == null || dto.getHashtagId() == null
                    || dto.getPostId().trim().isEmpty() || dto.getHashtagId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId & hashtagId are required"));
            }
            PostHashtag.PostHashtagId id = new PostHashtag.PostHashtagId(dto.getPostId().trim(), dto.getHashtagId().trim());
            PostHashtag link = postHashtagRepository.findById(id).orElse(null);
            if (link == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Liên kết không tồn tại"));
            }
            postHashtagRepository.delete(link);
            Hashtag hashtag = link.getHashtag();
            if (hashtag != null && hashtag.getPostCount() != null && hashtag.getPostCount() > 0) {
                hashtag.setPostCount(hashtag.getPostCount() - 1);
                hashtagRepository.save(hashtag);
            }
            logger.info("Detach hashtag {} from post {}. IP: {}", dto.getHashtagId(), dto.getPostId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DETACH HASHTAG SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in detach post-hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
