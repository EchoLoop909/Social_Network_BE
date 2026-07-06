package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.models.CreateUserRequestDTO;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.LoginRequest;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.UserStatus;
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
    public ResponseEntity<?> createUser(CreateUserRequestDTO req) {
        try {
            Optional<User> existingUser = userRepository.findByUsername(req.getUsername());
            if (existingUser.isPresent()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "User already exists "));
            }

            User newUser = new User();
            newUser.setUsername(req.getUsername());
            newUser.setEmail(req.getEmail());
            newUser.setName(req.getName());
            newUser.setSurname(req.getSurname());
            newUser.setDescription(req.getDescription());
            // Tài khoản mới luôn ở trạng thái chờ xác thực email (theo luồng đăng ký trong tài liệu)
            newUser.setStatus(UserStatus.PENDING_ACTIVATION);
            newUser.setFirstname(req.getFirstname());
            newUser.setLastname(req.getLastname());

            userRepository.save(newUser);

            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "INSERT SUCCESSFULLY"));

        } catch (Exception e) {
            logger.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

            if (userOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseMess(1, "INVALID USERNAME OR PASSWORD"));
            }

            return ResponseEntity.ok(userOpt.get());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMess(1, "SYSTEM ERROR"));
        }
    }

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
