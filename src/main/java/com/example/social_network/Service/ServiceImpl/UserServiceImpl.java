package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Repository.FollowershipRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.KeycloakAdminService;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Dto.UserUpdateDto;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.FollowStatus;
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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Autowired
    private FollowershipRepository followershipRepository;

    @Override
    public Object getUser(String userId, String keyword, String viewerId, int pageIdx, int pageSize) {
        try {
            Pageable paging = PageRequest.of(pageIdx, pageSize);

            // Chuẩn hóa: chuỗi rỗng -> null để bỏ qua điều kiện lọc tương ứng trong query.
            String userIdParam = (userId == null || userId.trim().isEmpty()) ? null : userId.trim();
            String keywordParam = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

            Page<User> usersPage = userRepository.findUser(userIdParam, keywordParam, paging);

            List<User> results = usersPage.getContent();

            // Ẩn user đã có quan hệ BLOCKED với người xem (mình chặn họ HOẶC họ chặn mình)
            if (viewerId != null && !results.isEmpty()) {
                Set<String> blocked = new HashSet<>(
                        followershipRepository.findBlockedRelatedIds(viewerId, FollowStatus.BLOCKED));
                if (!blocked.isEmpty()) {
                    results = new ArrayList<>(results);
                    results.removeIf(u -> blocked.contains(u.getId()));
                }
            }

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

    @Override
    @Transactional
    public ResponseEntity<?>  updateUser(String userId, UserUpdateDto dto, String ip) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            if (dto == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Dữ liệu cập nhật rỗng"));
            }
            if (dto.getDescription() != null && dto.getDescription().length() > 2000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Description vượt quá 2000 ký tự"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }

            // Partial update: chỉ cập nhật field khác null.
            if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
                user.setName(dto.getName().trim());
            }
            if (dto.getFirstname() != null && !dto.getFirstname().trim().isEmpty()) {
                user.setFirstname(dto.getFirstname().trim());
            }
            if (dto.getLastname() != null && !dto.getLastname().trim().isEmpty()) {
                user.setLastname(dto.getLastname().trim());
            }
            if (dto.getSurname() != null && !dto.getSurname().trim().isEmpty()) {
                user.setSurname(dto.getSurname().trim());
            }
            if (dto.getDescription() != null) {
                user.setDescription(dto.getDescription().trim());
            }
            if (dto.getPhoto() != null && !dto.getPhoto().trim().isEmpty()) {
                user.setPhoto(dto.getPhoto().trim());
            }
            if (dto.getIsPrivate() != null) {
                user.setIsPrivate(dto.getIsPrivate());
            }
            userRepository.save(user);

            logger.info("User {} updated profile successfully. IP: {}", userId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "UPDATE USER SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in updateUser: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteUser(DeleteDto dto, String ip) {
        try {
            if (dto == null || dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "userId is required"));
            }

            User user = userRepository.findById(dto.getId().trim()).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + dto.getId()));
            }
            if (user.getDeletionDate() != null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "User đã bị xóa trước đó"));
            }

            // Xóa mềm: giữ lại bản ghi + bài viết, chỉ đánh dấu thời điểm xóa và đình chỉ tài khoản.
            user.setDeletionDate(LocalDateTime.now());
            user.setStatus(UserStatus.SUSPENDED);
            userRepository.save(user);

            logger.info("Soft-deleted user {} successfully. IP: {}", dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE USER SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in deleteUser: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }

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

    @Override
    public ResponseEntity<?> syncUsersToKeycloak() {
        int created = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();
        try {
            String adminToken = keycloakAdminService.getAdminAccessToken();
            List<User> all = userRepository.findAll();
            logger.info("Bắt đầu đồng bộ {} user lên Keycloak", all.size());

            for (User u : all) {
                try {
                    RegisterRequest req = new RegisterRequest();
                    req.setUsername(u.getUsername());
                    req.setEmail(u.getEmail());
                    req.setName(u.getName());
                    req.setFirstname(u.getFirstname());
                    req.setLastname(u.getLastname());
                    req.setSurname(u.getSurname());
                    req.setPassword("123abc"); // mật khẩu mặc định theo yêu cầu

                    // Tạo user trên Keycloak (Keycloak tự sinh id/sub).
                    String sub = keycloakAdminService.createUser(req, adminToken);
                    // Đồng bộ id DB = sub Keycloak (FK đã có ON UPDATE CASCADE nên lan xuống bảng con).
                    if (sub != null && !sub.equals(u.getId())) {
                        userRepository.updateUserId(sub, u.getId());
                    }
                    created++;
                } catch (HttpStatusCodeException ex) {
                    if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                        // User đã tồn tại trên Keycloak -> tra id thật rồi đồng bộ id DB cho khớp (idempotent).
                        try {
                            String kcId = keycloakAdminService.findUserIdByUsername(u.getUsername(), adminToken);
                            if (kcId != null && !kcId.equals(u.getId())) {
                                userRepository.updateUserId(kcId, u.getId());
                                created++;
                            } else {
                                skipped++; // đã khớp sẵn hoặc không tra được -> bỏ qua
                            }
                        } catch (Exception re) {
                            failed++;
                            errors.add(u.getUsername() + " (reconcile): " + re.getMessage());
                        }
                    } else {
                        failed++;
                        errors.add(u.getUsername() + ": " + ex.getStatusCode());
                    }
                } catch (Exception ex) {
                    failed++;
                    errors.add(u.getUsername() + ": " + ex.getMessage());
                }
            }

            String mess = "SYNC KEYCLOAK DONE - created=" + created
                    + ", skipped=" + skipped + ", failed=" + failed
                    + (errors.isEmpty() ? "" : " | errors: " + String.join("; ", errors));
            logger.info(mess);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, mess));

        } catch (Exception e) {
            logger.error("Lỗi đồng bộ Keycloak: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
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
