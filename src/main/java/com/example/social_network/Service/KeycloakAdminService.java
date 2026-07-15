package com.example.social_network.Service;

import com.example.social_network.Payload.Request.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gọi Admin REST API của Keycloak phục vụ luồng đăng ký:
 *   ① lấy admin token (client_credentials)
 *   ② tạo user (emailVerified=true) -> trả về userId (sub)
 *   (rollback) xóa user khi lưu DB thất bại
 */
@Service
public class KeycloakAdminService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private String tokenUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    private String usersUrl() {
        return serverUrl + "/admin/realms/" + realm + "/users";
    }

    private String logoutUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    /** Đăng nhập bằng username/password (Direct Access Grant). Trả về map chứa access_token, refresh_token... */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> passwordLogin(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", username);
        form.add("password", password);
        form.add("scope", "openid");

        ResponseEntity<Map> resp = restTemplate.postForEntity(
                tokenUrl(), new HttpEntity<>(form, headers), Map.class);
        return (Map<String, Object>) resp.getBody();
    }

    /** Đăng xuất: thu hồi phiên/refresh_token tại Keycloak. */
    public void logout(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        restTemplate.postForEntity(logoutUrl(), new HttpEntity<>(form, headers), Void.class);
    }

    /** ① Lấy access token quyền admin bằng client_credentials. */
    @SuppressWarnings("rawtypes")
    public String getAdminAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        ResponseEntity<Map> resp = restTemplate.postForEntity(
                tokenUrl(), new HttpEntity<>(form, headers), Map.class);

        if (resp.getBody() == null || resp.getBody().get("access_token") == null) {
            throw new IllegalStateException("Không lấy được admin token từ Keycloak");
        }
        return resp.getBody().get("access_token").toString();
    }

    /**
     * ② Tạo user trong Keycloak (Keycloak tự sinh id). Dùng cho luồng đăng ký.
     * @return userId (sub) lấy từ header Location của response 201.
     * Ném HttpClientErrorException.Conflict (409) nếu Keycloak báo trùng username/email.
     */
    public String createUser(RegisterRequest req, String adminToken) {
        return createUser(req, adminToken, null);
    }

    /**
     * ② Tạo user trong Keycloak, có thể chỉ định trước id (desiredId).
     * Khi desiredId != null, gửi kèm field "id" để Keycloak dùng luôn id này
     * -> đảm bảo users.id_user (DB) == id user trên Keycloak khi đồng bộ, không phải đổi khóa chính.
     * Khi desiredId == null, Keycloak tự sinh id như thường.
     * @return userId (sub) lấy từ header Location của response 201.
     */
    public String createUser(RegisterRequest req, String adminToken, String desiredId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", req.getPassword(),
                "temporary", false
        );

        Map<String, Object> body = new HashMap<>();
        if (StringUtils.hasText(desiredId)) {
            body.put("id", desiredId);
        }
        body.put("username", req.getUsername());
        body.put("email", req.getEmail());
        body.put("firstName", req.getFirstname());
        body.put("lastName", req.getLastname());
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("credentials", List.of(credential));

        ResponseEntity<Void> resp = restTemplate.postForEntity(
                usersUrl(), new HttpEntity<>(body, headers), Void.class);

        String location = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (!StringUtils.hasText(location)) {
            throw new IllegalStateException("Keycloak không trả về Location (userId) sau khi tạo user");
        }
        String userId = location.substring(location.lastIndexOf('/') + 1);
        logger.info("Keycloak đã tạo user, sub = {}", userId);
        return userId;
    }

    /**
     * Tra id (sub) của user trên Keycloak theo username (khớp chính xác).
     * Dùng khi đồng bộ: user đã tồn tại trên Keycloak (409) -> lấy id thật để đồng bộ id DB cho khớp.
     * @return id user trên Keycloak, hoặc null nếu không tìm thấy.
     */
    @SuppressWarnings("rawtypes")
    public String findUserIdByUsername(String username, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        String url = usersUrl() + "?exact=true&username="
                + URLEncoder.encode(username, StandardCharsets.UTF_8);
        ResponseEntity<List> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), List.class);

        List body = resp.getBody();
        if (body == null || body.isEmpty()) return null;
        Object first = body.get(0);
        if (first instanceof Map) {
            Object id = ((Map<?, ?>) first).get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /** Rollback: xóa user vừa tạo ở Keycloak khi bước lưu DB thất bại. */
    public void deleteUser(String userId, String adminToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            restTemplate.exchange(
                    usersUrl() + "/" + userId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class);
            logger.warn("Đã rollback: xóa user {} trên Keycloak", userId);
        } catch (Exception e) {
            logger.error("Rollback thất bại, user {} có thể còn trên Keycloak: {}", userId, e.getMessage());
        }
    }
}
