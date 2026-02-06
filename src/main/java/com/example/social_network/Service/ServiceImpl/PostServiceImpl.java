package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Response.Message;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Payload.Util.Status;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostService;
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

//Optional<User> user = userRepository.findByUserId(Long.valueOf(userId));
//            if(user.isEmpty()){
//        return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "User not found "));
//        }
//
//Optional<Post> post = postRepository.findByPostId(postId);
//            if(post.isEmpty()){
//        return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "Post not found "));
//        }
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
    }
