package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.KeycloakAdminService;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

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

    /**
     * Luồng đăng ký (ĐÃ BỎ xác thực email — tài khoản ACTIVE ngay):
     *  B2. Kiểm tra trùng email/username trong DB
     *  B3. Lấy admin token Keycloak
     *  B4. Tạo user trên Keycloak (emailVerified=true, lấy sub)
     *  B5. Lưu bảng users với id = sub, status = ACTIVE (rollback Keycloak nếu lỗi)
     */
    @Override
    public ResponseEntity<?> register(RegisterRequest req) {
        // B2 - kiểm tra trùng lặp
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseHelper.buildFalseResponse(HttpStatus.CONFLICT,
                    new ResponseMess(1, "Email đã tồn tại"));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseHelper.buildFalseResponse(HttpStatus.CONFLICT,
                    new ResponseMess(1, "Username đã tồn tại"));
        }

        String userId;
        String adminToken;
        try {
            // B3 - lấy admin token
            adminToken = keycloakAdminService.getAdminAccessToken();
        } catch (Exception e) {
            logger.error("Không lấy được admin token Keycloak: {}", e.getMessage());
            return ResponseHelper.buildFalseResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Không kết nối được máy chủ xác thực"));
        }

        try {
            // B4 - tạo user trên Keycloak
            userId = keycloakAdminService.createUser(req, adminToken);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseHelper.buildFalseResponse(HttpStatus.CONFLICT,
                        new ResponseMess(1, "Email hoặc username đã tồn tại trên hệ thống xác thực"));
            }
            logger.error("Keycloak tạo user lỗi {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseHelper.buildFalseResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Tạo tài khoản trên máy chủ xác thực thất bại"));
        } catch (Exception e) {
            logger.error("Keycloak tạo user lỗi: {}", e.getMessage());
            return ResponseHelper.buildFalseResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Tạo tài khoản trên máy chủ xác thực thất bại"));
        }

        try {
            // B5 - lưu DB (không xác thực email: tài khoản ACTIVE ngay)
            User user = new User();
            user.setId(userId); // id_user = sub của Keycloak
            user.setUsername(req.getUsername());
            user.setEmail(req.getEmail());
            user.setName(req.getName());
            user.setFirstname(req.getFirstname());
            user.setLastname(req.getLastname());
            user.setSurname(req.getSurname());
            user.setStatus(UserStatus.ACTIVE);
            user.setIsChecked(true);
            userRepository.save(user);

            logger.info("Đăng ký thành công user {} (sub={})", req.getUsername(), userId);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("status", UserStatus.ACTIVE.name());
            data.put("message", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
            return ResponseHelper.buildResponse(data, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Lưu DB thất bại, rollback user {} trên Keycloak. Lỗi: {}", userId, e.getMessage());
            keycloakAdminService.deleteUser(userId, adminToken);
            return ResponseHelper.buildFalseResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Lưu thông tin người dùng thất bại, vui lòng thử lại"));
        }
    }

    /**
     * Đăng nhập (Cách 2 - backend proxy):
     *  - gọi Keycloak Direct Access Grant lấy token
     *  - đọc sub + email_verified từ access_token → chặn SUSPENDED / sync ACTIVE (bước 3 docs)
     *  - trả access_token + refresh_token + thông tin user
     */
    @Override
    @Transactional
    public ResponseEntity<?> login(LoginRequest req) {
        Map<String, Object> token;
        try {
            token = keycloakAdminService.passwordLogin(req.getUsername(), req.getPassword());
        } catch (HttpStatusCodeException e) {
            // sai mật khẩu hoặc tài khoản bị vô hiệu hóa ở Keycloak
            logger.warn("Đăng nhập thất bại cho '{}': {}", req.getUsername(), e.getResponseBodyAsString());
            return ResponseHelper.buildFalseResponse(HttpStatus.UNAUTHORIZED,
                    new ResponseMess(1, "Sai tài khoản hoặc mật khẩu"));
        } catch (Exception e) {
            logger.error("Lỗi kết nối Keycloak khi đăng nhập: {}", e.getMessage());
            return ResponseHelper.buildFalseResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Không kết nối được máy chủ xác thực"));
        }

        String accessToken = String.valueOf(token.get("access_token"));
        Map<String, Object> claims = decodeJwtPayload(accessToken);
        String sub = (String) claims.get("sub");
        boolean emailVerified = Boolean.TRUE.equals(claims.get("email_verified"));

        User user = null;
        if (sub != null) {
            Optional<User> userOpt = userRepository.findById(sub);
            if (userOpt.isPresent()) {
                user = userOpt.get();
                // bước 3 docs: kiểm tra trạng thái tài khoản
                if (user.getStatus() == UserStatus.SUSPENDED) {
                    keycloakAdminService.logout(String.valueOf(token.get("refresh_token"))); // thu hồi token vừa cấp
                    return ResponseHelper.buildFalseResponse(HttpStatus.FORBIDDEN,
                            new ResponseMess(1, "Tài khoản đang bị khóa"));
                }
                // sync-on-login: đã verify email mà còn chờ kích hoạt -> ACTIVE
                if (emailVerified && user.getStatus() == UserStatus.PENDING_ACTIVATION) {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setIsChecked(true);
                    userRepository.save(user);
                    logger.info("Sync-on-login: kích hoạt {} -> ACTIVE", sub);
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("access_token", token.get("access_token"));
        data.put("refresh_token", token.get("refresh_token"));
        data.put("expires_in", token.get("expires_in"));
        data.put("token_type", token.get("token_type"));
        data.put("user", user);
        return ResponseHelper.buildResponse(data, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> logout(String refreshToken) {
        try {
            keycloakAdminService.logout(refreshToken);
        } catch (Exception e) {
            logger.warn("Đăng xuất gặp lỗi (bỏ qua): {}", e.getMessage());
        }
        return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                new ResponseMess(0, "Đăng xuất thành công"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return Collections.emptyMap();
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(payload, Map.class);
        } catch (Exception e) {
            logger.warn("Không giải mã được payload JWT: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
