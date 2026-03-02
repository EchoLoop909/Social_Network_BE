package com.example.social_network.Config;

import com.example.social_network.Repository.StatusRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.models.Entity.Statususer;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(CustomJwtAuthenticationConverter.class);

    // Sử dụng Set thread-safe để lưu trữ tạm thời các keycloakId đã được đồng bộ
    // Giúp giảm tải Database cho các request liên tục từ cùng 1 user
    private final Set<String> syncedUsersCache = ConcurrentHashMap.newKeySet();

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;

    public CustomJwtAuthenticationConverter(UserRepository userRepository, StatusRepository statusRepository) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Thực hiện kiểm tra và đồng bộ User
        syncUserIfNeeded(jwt);

        // 2. Trả về Authentication Token cho Spring Security xử lý tiếp
        return new JwtAuthenticationToken(jwt);
    }

    private void syncUserIfNeeded(Jwt jwt) {
        String keycloakId = jwt.getClaimAsString("sub");

        // Nếu ID này đã có trong RAM cache, bỏ qua luôn không chạm vào DB
        if (syncedUsersCache.contains(keycloakId)) {
            return;
        }

        // Nếu chưa có trong Cache, kiểm tra dưới Database
        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);
        if (existingUser.isPresent()) {
            // Nếu có trong DB rồi, nạp vào Cache để lần sau chạy nhanh hơn
            syncedUsersCache.add(keycloakId);
            return;
        }

        // Nếu chưa có ở cả Cache và DB -> Tiến hành tạo mới
        try {
            // Lấy thông tin mặc định từ Keycloak Token
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");

            // FIX 1: Truyền String vào findById (Bạn nhớ đổi "1" thành ID đúng trong DB của bạn nhé)
            Statususer status = statusRepository.findById("1")
                    .orElseThrow(() -> new RuntimeException("Status không tồn tại"));

            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFirstname(firstName);
            newUser.setLastname(lastName);

            // FIX 2: Đưa .trim() vào bên trong hàm setName()
            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            newUser.setName(fullName.trim());

            newUser.setStatus(status);

            userRepository.save(newUser);

            // Lưu thành công thì đưa vào Cache
            syncedUsersCache.add(keycloakId);
            logger.info("Đã đồng bộ user mới từ Keycloak vào DB thành công: {}", username);

        } catch (Exception e) {
            logger.error("Lỗi khi đồng bộ user từ Keycloak: {}", e.getMessage());
        }
    }
}