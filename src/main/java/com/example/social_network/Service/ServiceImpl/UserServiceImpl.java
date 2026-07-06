package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.ResponseMess;
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

import javax.transaction.Transactional;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public Object getUser(String userId, int pageIdx, int pageSize) {
        try {
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<User> usersPage;

            if (userId == null || userId.isEmpty()) {
                usersPage = userRepository.findAll(paging);
            } else {
                usersPage = userRepository.findUser(userId, paging);
            }

            List<User> results = usersPage.getContent();

            logger.info("get list user success page: " + pageIdx + " size: " + pageSize);
            return ResponseHelper.getResponses(
                    results,
                    usersPage.getTotalElements(),
                    usersPage.getTotalPages(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> uploadImg(String username, String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(
                        HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Photo URL is required")
                );
            }
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseHelper.getResponseSearchMess(
                        HttpStatus.NOT_FOUND,
                        new ResponseMess(1, "User not found")
                );
            }
            User user = userOpt.get();
            user.setPhoto(url);
            userRepository.save(user);
            return ResponseHelper.getResponseSearchMess(
                    HttpStatus.OK,
                    new ResponseMess(0, "UPDATE AVATAR SUCCESSFULLY")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

}
