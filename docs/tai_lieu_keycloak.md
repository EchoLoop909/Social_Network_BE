# TÀI LIỆU HƯỚNG DẪN CẤU HÌNH VÀ TÍCH HỢP XÁC THỰC KEYCLOAK

Tài liệu này hướng dẫn chi tiết cách thiết lập Keycloak làm hệ thống quản lý định danh (Identity Provider - IdP) và tích hợp vào dự án Spring Boot Backend để xác thực (Authentication) và phân quyền (Authorization) cho mạng xã hội.

---

## 1. KIẾN TRÚC XÁC THỰC HỆ THỐNG (OAUTH2 / OIDC)

Hệ thống hoạt động theo mô hình **OAuth 2.0 / OpenID Connect (OIDC)** với các thành phần:
1. **Identity Provider (Keycloak Server)**: Đóng vai trò Authorization Server, chịu trách nhiệm quản lý User, mật khẩu, sinh mã thông báo truy cập (JWT Access Token) và kiểm soát phiên làm việc.
2. **Resource Server (Spring Boot Backend)**: Đóng vai trò bảo vệ các tài nguyên API (Post, Comment, Message...). Backend sẽ cấu hình tự động tải các khóa công khai (JWKS) từ Keycloak để xác thực chữ ký của JWT Token gửi lên từ Client mà không cần gọi lại Keycloak cho mỗi request.
3. **Client App (Frontend Web/Mobile)**: Nhận Access Token sau khi đăng nhập thành công và đính kèm Token vào Header (`Authorization: Bearer <token>`) khi gọi API.

```
+------------+             (1) Đăng nhập              +------------+
|            |--------------------------------------->|            |
|  Frontend  |<---------------------------------------|  Keycloak  |
|  (Client)  |             Trả về JWT Token           |   (IdP)    |
+------------+                                        +------------+
      |
      | (2) Gửi Request kèm Token
      |     Header: Authorization: Bearer <JWT>
      v
+------------+
|            |
| SpringBoot |<======================================== Verify ký số offline bằng JWKS URI
|  Backend   |
+------------+
```

---

## 2. HƯỚNG DẪN CẤU HÌNH TRÊN ADMIN CONSOLE KEYCLOAK

### Bước 2.1: Tạo Realm mới
1. Đăng nhập vào trang quản trị Keycloak (mặc định: `http://localhost:8080/admin`).
2. Nhấp vào danh sách Realm ở góc trên bên trái và chọn **Create Realm**.
3. Đặt tên Realm (ví dụ: `social-network`) và nhấn **Create**.

### Bước 2.2: Tạo và cấu hình Client
Client là đại diện ứng dụng của bạn đăng ký với Keycloak.
1. Truy cập menu **Clients** $\rightarrow$ chọn **Create client**.
2. Điền thông tin:
   - **Client type**: `OpenID Connect`
   - **Client ID**: `social-network-api`
   - **Name**: `Social Network Backend API`
3. Nhấp **Next**. Cấu hình **Capability config**:
   - Bật **Client authentication** (để sử dụng Client Secret).
   - Chọn **Authorization** nếu muốn dùng phân quyền nâng cao.
   - **Authentication flow**: Chọn cả **Standard flow** (cho Authorization Code Flow) và **Direct access grants** (để test luồng password nếu cần).
4. Nhấp **Save**.
5. Trong tab **Credentials**, sao chép giá trị **Client Secret** (sẽ dùng cấu hình trong file `application.properties` của Spring Boot).
6. Trong cấu hình **Access settings**:
   - **Valid redirect URIs**: Điền địa chỉ Frontend (ví dụ: `http://localhost:3000/*`) hoặc `*` cho môi trường phát triển.
   - **Web Origins**: Điền `*` để tránh lỗi CORS.

### Bước 2.3: Tạo User kiểm thử
1. Truy cập menu **Users** $\rightarrow$ chọn **Add user**.
2. Nhập thông tin: `username`, `email`, `first name`, `last name`. Nhấn **Create**.
3. Chuyển qua tab **Credentials**, chọn **Set password**.
4. Nhập mật khẩu kiểm thử và **tắt** nút *Temporary* (để tránh yêu cầu đổi mật khẩu ở lần đăng nhập đầu tiên).

---

## 3. CẤU HÌNH SPRING BOOT BACKEND (RESOURCE SERVER)

### Bước 3.1: Thêm thư viện vào `pom.xml`
Cấu hình Spring Boot làm OAuth2 Resource Server để tự động nhận diện và validate JWT từ Keycloak:

```xml
<dependencies>
    <!-- OAuth2 Resource Server để tự động validate JWT Token -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

### Bước 3.2: Cấu hình file `application.properties`
Cấu hình kết nối tới server Keycloak của bạn:

```properties
# URL máy chủ Keycloak
keycloak.auth-server-url=http://localhost:8080
keycloak.realm=social-network

# Thông tin Client ID cấu hình trên Keycloak
idp.client-id=social-network-api
idp.client-secret=YOUR_CLIENT_SECRET_FROM_KEYCLOAK

# Cấu hình Spring OAuth2 Resource Server để tự động validate JWT Token
spring.security.oauth2.resourceserver.jwt.issuer-uri=${keycloak.auth-server-url}/realms/${keycloak.realm}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/certs
```

### Bước 3.3: Lớp cấu hình Security (SecurityConfig)
Lớp này cấu hình các Endpoint nào công khai (Public) và Endpoint nào bắt buộc phải xác thực Token:

```java
package com.example.social_network.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests(auth -> auth
                // Cấu hình các API công khai không cần Token
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/api/posts/**").permitAll() // Xem bài viết công khai
                // Tất cả các API còn lại bắt buộc phải có Bearer Token hợp lệ
                .anyRequest().authenticated()
            )
            // Cấu hình ứng dụng đóng vai trò Resource Server xác thực JWT
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
            
        return http.build();
    }
}
```

---

## 4. CHI TIẾT CÁC API VÀ LUỒNG ĐỒNG BỘ USER

### 4.1. Luồng Đăng nhập lấy Token (API Backend làm Proxy)
Nếu hệ thống của bạn chọn phương án Backend làm Proxy nhận mật khẩu và đổi Token từ Keycloak (Luồng Password Grant):

* **Endpoint**: `POST /api/auth/login`
* **Request Body**:
```json
{
  "username": "testuser",
  "password": "mypassword"
}
```
* **Logic xử lý tại Backend (`UserServiceImpl.java`)**:
```java
public ResponseEntity<?> login(LoginRequest request) {
    String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("username", request.getUsername());
    body.add("password", request.getPassword());

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
    
    return ResponseEntity.ok(response.getBody()); // Trả về Access Token, Refresh Token cho Frontend
}
```

---

### 4.2. Luồng Đồng bộ tài khoản sang Cơ sở dữ liệu chính
Sau khi Frontend nhận được `access_token` từ Keycloak, Frontend sẽ gửi Token này trong Header của tất cả request tiếp theo. Ở lần đăng nhập đầu tiên, Frontend sẽ gọi một API "Đồng bộ" `/api/users/sync` để lưu thông tin từ Keycloak sang MySQL:

* **Endpoint**: `POST /api/users/sync`
* **Header**: `Authorization: Bearer <access_token>`
* **Logic xử lý tại Backend (`UserServiceImpl.java`)**:
Backend sử dụng đối tượng `Jwt` được Spring Security tự động giải mã từ token để lấy trường `sub` (Keycloak ID duy nhất) và lưu dữ liệu:

```java
@Override
public ResponseEntity<?> createUser(CreateUserRequestDTO req, Jwt jwt) {
    // Lấy ID định danh độc nhất (UUID) của User từ trường "sub" trong JWT Token
    String keycloakId = jwt.getClaimAsString("sub");

    // Kiểm tra xem User này đã được đồng bộ trong DB MySQL chưa
    Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);
    if (existingUser.isPresent()) {
        return ResponseEntity.ok(new ResponseMess(0, "User đã tồn tại, không cần đồng bộ."));
    }

    // Tiến hành lưu thông tin User mới vào MySQL
    User newUser = new User();
    newUser.setId(keycloakId); // Thiết lập Khóa chính trùng với Keycloak ID
    newUser.setKeycloakId(keycloakId);
    newUser.setUsername(req.getUsername());
    newUser.setEmail(req.getEmail());
    newUser.setName(req.getName());
    newUser.setFirstname(req.getFirstname());
    newUser.setLastname(req.getLastname());
    
    userRepository.save(newUser);
    return ResponseEntity.ok(new ResponseMess(0, "ĐỒNG BỘ USER THÀNH CÔNG"));
}
```

---

## 5. CÁCH LẤY THÔNG TIN USER ĐANG ĐĂNG NHẬP TRONG CONTROLLER
Spring Security tự động đưa thông tin Token đã xác thực vào `SecurityContext`. Tại bất cứ Controller nào, bạn chỉ cần tiêm annotation `@AuthenticationPrincipal Jwt jwt` để lấy thông tin User hiện tại:

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody PostRequestDTO req, 
                                        @AuthenticationPrincipal Jwt jwt) {
        // Lấy Keycloak ID của User đang đăng nhập
        String currentUserId = jwt.getClaimAsString("sub"); 
        
        // Tiến hành tạo bài viết liên kết với User ID này
        return postService.createPost(req, currentUserId);
    }
}
```
