package com.example.social_network.Config;

import com.example.social_network.Repository.StatusRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.models.Entity.Statususer;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(CustomJwtAuthenticationConverter.class);

    private final Set<String> syncedUsersCache = ConcurrentHashMap.newKeySet();
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;

    // BƯỚC 1: Khai báo bộ chuyển đổi quyền mặc định của Spring
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public CustomJwtAuthenticationConverter(UserRepository userRepository, StatusRepository statusRepository) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Thực hiện kiểm tra và đồng bộ User
        syncUserIfNeeded(jwt);

        // BƯỚC 2: Trích xuất danh sách quyền từ Token
        Collection<GrantedAuthority> authorities = defaultAuthoritiesConverter.convert(jwt);

        // BƯỚC 3: Dùng constructor 2 tham số. Lúc này isAuthenticated sẽ được set = true!
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private void syncUserIfNeeded(Jwt jwt) {
        String keycloakId = jwt.getClaimAsString("sub");

        if (syncedUsersCache.contains(keycloakId)) {
            return;
        }

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);
        if (existingUser.isPresent()) {
            syncedUsersCache.add(keycloakId);
            return;
        }

        try {
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");

            Statususer status = statusRepository.findById("1")
                    .orElseThrow(() -> new RuntimeException("Status không tồn tại"));

            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFirstname(firstName);
            newUser.setLastname(lastName);

            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            newUser.setName(fullName.trim());
            newUser.setStatus(status);

            userRepository.save(newUser);

            syncedUsersCache.add(keycloakId);
            logger.info("Đã đồng bộ user mới từ Keycloak vào DB thành công: {}", username);

        } catch (Exception e) {
            logger.error("Lỗi khi đồng bộ user từ Keycloak: {}", e.getMessage());
        }
    }
}