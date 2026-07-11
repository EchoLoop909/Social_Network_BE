# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Đây là repo **backend** của project SocialNetwork. Repo frontend nằm ở thư mục ngang hàng `../Social_Network_FE`.

## Lệnh thường dùng

Wrapper là `mvnw.cmd` trên Windows (`./mvnw` khi dùng Git Bash).
- Chạy server dev: `mvnw.cmd spring-boot:run` — chạy ở **cổng 8888**
- Đóng gói WAR: `mvnw.cmd clean package` → `target/springboot.war`
- Chạy toàn bộ test: `mvnw.cmd test`
- Chạy một test: `mvnw.cmd test -Dtest=SocialNetworkApplicationTests#someMethod`

> App **sẽ không khởi động** nếu các phụ thuộc bên ngoài không sẵn sàng: DB MySQL (`social_clone`) và realm Keycloak được khai báo trong `spring.security.oauth2.resourceserver.jwt.issuer-uri` (Spring xác thực issuer ngay lúc boot). Chuỗi kết nối DB, client secret của Keycloak và key Cloudinary hiện đang hardcode trong `src/main/resources/application.properties`.

## Kiến trúc Backend

Package gốc `com.example.social_network`. Đây là Spring Boot **2.x**, nên import dùng `javax.*` (ví dụ `javax.servlet`, `javax.validation`), **không phải** `jakarta.*`.

**Phân tầng:** `Controller` → `Service` (interface) → `Service/ServiceImpl` → `Repository` (Spring Data JPA). Entity trong `models/Entity`, enum trong `models/Enum`, DTO request/response chia giữa `models/Dto` và `Payload/Request`|`Payload/Response`.

**Envelope phản hồi (quan trọng):** endpoint không trả về DTO trần. Chúng trả về một `Map` do `ResHelper/ResponseHelper` dựng với các trường `Status`, `Errors`, `isOk`, `isError`, `Object` (payload), đôi khi kèm `totalPage`/`totalElements`. FE đọc payload từ `res.data.Object`. Khi thêm endpoint mới, hãy dùng lại `ResponseHelper.getResponses(...)` v.v. thay vì trả object thô. Lỗi validate dùng cấu trúc khác (`getErrorResponse(BindingResult, ...)` → `errors`, `hasErrors`, `timestamp`).

**Hằng số route:** tất cả các đoạn đường dẫn URL là hằng chuỗi trong `Payload/Util/PathResources.java`, ghép qua `@RequestMapping(PathResources.X)`. Lưu ý một số mapping không trực quan — ví dụ `PostController` map dưới `/images` (lấy danh sách bài viết là `GET /images/post`, upload là `POST /images/upload`). Hãy kiểm tra `PathResources` trước khi đoán route.

**Xác thực (dựa trên Keycloak):** BE là một OAuth2 **resource server** — nó xác thực JWT do Keycloak cấp. `SecurityConfig` yêu cầu mọi request phải có Bearer token trừ `/auth/register|login|refresh|logout`, `/auth/activate/**`, và swagger. Session ở chế độ STATELESS.
- `KeycloakAdminService` gọi Admin REST API + token endpoint của Keycloak. Đăng ký do backend điều phối: BE lấy admin token, tạo user trên Keycloak, rồi lưu một dòng vào bảng `users` với `id_user = sub` (subject của Keycloak chính là khóa chính). Nếu ghi DB thất bại thì rollback bằng cách xóa user vừa tạo trên Keycloak.
- Login/refresh/logout được proxy qua BE tới Keycloak bằng **ROPC / password grant**.
- **Xác thực email bị tắt có chủ đích** — user mới được tạo với `emailVerified=true` và `status=ACTIVE` ngay. `SUSPENDED` là trạng thái mức ứng dụng do BE kiểm tra lúc login/`/auth/me`, không phải cờ của Keycloak. Xem `docs/note.md` để hiểu đầy đủ lý do và danh sách TODO (login Google, quên mật khẩu, admin khóa tài khoản đều chưa làm).
- Định danh user hiện tại lấy từ JWT: `jwt.getSubject()` = id user.

**Phụ thuộc hạ tầng đã khai báo nhưng chưa nối hết:** Kafka, Redis, và WebSocket (STOMP, endpoint `/ws`, prefix broker `/topic` `/queue`, prefix app `/app` trong `WebSocketConfig`) đều có trên classpath. **Pipeline nhắn tin Kafka cùng `MessageServiceImpl`/`KafkaMessageListener` hiện đang bị comment toàn bộ** — chức năng nhắn tin chưa hoạt động. Upload media đi qua **Cloudinary** trong `Config/Cloudinary/CloudinaryService`.
