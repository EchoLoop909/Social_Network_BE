# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Đây là repo **backend** của project SocialNetwork. Repo frontend nằm ở thư mục ngang hàng `../Social_Network_FE`.

## ⚠️ BẮT BUỘC: Đọc tài liệu trong `docs/` trước khi làm bất kỳ việc gì

Trước khi thực hiện **bất kỳ yêu cầu/lệnh nào** liên quan tới code (viết code, sửa code, thêm API, đối chiếu code với đặc tả, giải thích nghiệp vụ, thiết kế DB...), **PHẢI đọc tài liệu đặc tả** để bám đúng nghiệp vụ đồ án, không tự suy đoán.

**Tài liệu đặc tả CHÍNH THỨC — chỉ đọc đúng file này:** `docs/2121050669_Hoàng_Hiệp_da_sua.docx` (quyển đồ án tốt nghiệp).

> **KHÔNG đọc** hai file docx còn lại — `..._BODAXACTHUC.docx` và `...BACKUP.docx` — chúng là bản sao/nháp cũ, chỉ gây nhiễu.

**Hai file `.md` bổ trợ** (đọc thêm khi đụng đúng chủ đề, không bắt buộc mỗi lần):

| File | Nội dung |
|---|---|
| `docs/note.md` | Ghi chú luồng đăng ký/đăng nhập Keycloak theo code thực tế |
| `docs/bang_mo_ta_du_lieu.md` | Data dictionary — 17 bảng, ánh xạ 1-1 với Entity |

**Cách đọc file `.docx`** (file nhị phân, không đọc trực tiếp bằng Read): giải nén `word/document.xml` rồi lọc thẻ. Ví dụ bằng PowerShell:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead("docs\<ten_file>.docx")
$e = $zip.Entries | Where-Object { $_.FullName -eq "word/document.xml" }
$xml = (New-Object System.IO.StreamReader($e.Open())).ReadToEnd(); $zip.Dispose()
($xml -replace '</w:p>', "`n" -replace '<[^>]+>','') | Out-File docx_text.txt -Encoding utf8
```

**Khi tài liệu mâu thuẫn với code:** lấy `da_sua.docx` làm chuẩn đặc tả và **báo lại cho người dùng** phần code lệch, thay vì tự quyết. Lưu ý riêng: mục **Auth** trong `da_sua.docx` vẫn mô tả xác thực email + 3 trạng thái, trong khi code thật đã bỏ xác thực email (xem `note.md`) — khi làm phần Auth thì đối chiếu thêm `note.md`. **KHÔNG được tự ý sửa/xóa file trong `docs/`.**

## ⚠️ BẮT BUỘC: Xin phép trước khi sửa code

**KHÔNG được tự ý tạo mới / sửa / xóa bất kỳ file code nào.** Trước mọi thay đổi code, PHẢI:
1. Trình bày rõ **kế hoạch**: sẽ đụng file nào, thêm/sửa/xóa gì, vì sao.
2. **Chờ người dùng đồng ý rõ ràng** ("ok", "làm đi", "đồng ý"...) rồi mới thực hiện.

Người dùng muốn **review trước** mọi thay đổi. Được phép làm ngay mà không cần hỏi: đọc/phân tích/giải thích code, chạy lệnh **chỉ đọc** (build/test/query SELECT). Mọi thao tác **ghi** (sửa file code, ghi/xóa DB, chạy migration) đều phải xin phép trước. Quy tắc này áp dụng cho **cả repo BE lẫn FE**.

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
