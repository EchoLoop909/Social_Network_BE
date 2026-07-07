# GHI CHÚ — LUỒNG ĐĂNG KÝ & ĐĂNG NHẬP (Auth Flow)

> File ghi lại điểm cần lưu ý khi triển khai/vận hành luồng đăng ký + đăng nhập với Keycloak.
> Bao gồm phần GIỐNG / KHÁC so với tài liệu mẫu (đồ án) và các điểm chưa sát tài liệu.
> Cập nhật theo code hiện tại: backend Spring Boot (resource server) + Keycloak + Mailtrap.

---

# PHẦN A — LUỒNG ĐĂNG KÝ (Register)

## A1. Tổng quan

Đăng ký theo **Cách B** (Backend điều phối): Client gọi 1 API của Backend; Backend gọi Admin REST API của Keycloak.

```
[Client] POST /auth/register
  → [Backend] validate → check trùng DB → Keycloak (lấy admin token → tạo user → gửi mail xác thực)
  → lưu bảng users (status = PENDING_ACTIVATION, id_user = sub)
  → trả "kiểm tra email"

[User] bấm link trong mail → Keycloak set emailVerified = true
[User đăng nhập lần đầu] → Backend đọc email_verified → set ACTIVE (sync-on-login)
```

Số call: user thấy 1 API (`/auth/register`); bên trong Backend gọi Keycloak 3 call (token / create user / execute-actions-email) + 1 ghi DB.

## A2. GIỐNG tài liệu mẫu ✅

| Bước docs | Code |
|---|---|
| 1. Nhập Email/Username/Mật khẩu, gửi Backend | `POST /auth/register` + `RegisterRequest` |
| 2. Validate định dạng, sai → báo lỗi | `@Valid` + `BindingResult` → 400 |
| 3. Kiểm tra trùng Email → gọi Keycloak tạo user | `existsByEmail` (+ `existsByUsername`) → 409 |
| 4. Keycloak tạo account + Verify Email + gửi mail; Backend lưu DB PENDING_ACTIVATION | `createUser` → `sendVerifyEmail` → `save` (id = sub) |

Ánh xạ DB đúng bảng mô tả dữ liệu: `id_user = sub`, insert đủ cột NN.

## A3. KHÁC tài liệu mẫu ⚠️

| | Docs | Code |
|---|---|---|
| Bước 5 — kích hoạt ACTIVE | **Webhook**: Keycloak bắn sự kiện verify → Backend set ACTIVE ngay khi bấm link | **Sync-on-login (cách a)**: set ACTIVE khi user đăng nhập lần đầu sau verify |
| Gửi mail | Docs nói Keycloak "tự động gửi" | Code chủ động gọi `execute-actions-email` (đúng thực tế, vì user tạo qua Admin API không tự gửi) |
| Rollback | Không đề cập | Code có: lưu DB lỗi → xóa user Keycloak (tránh user mồ côi) |

## A4. Chưa sát tài liệu / chưa làm

1. **Webhook kích hoạt (bước 5 docs)**: chưa làm — đang thay bằng sync-on-login.
2. **Mật khẩu yếu**: Keycloak trả 400 nhưng code gộp vào 500 chung chung — nên map lại thành 400 + thông báo rõ.

---

# PHẦN B — LUỒNG ĐĂNG NHẬP (Login)

## B1. Tổng quan (Cách 2 — Backend proxy)

FE gọi Backend, Backend proxy sang Keycloak (Direct Access Grant / password grant).

```
[FE] POST /auth/login { username, password }
  → [Backend] gọi Keycloak token endpoint (grant_type=password)
       • sai mật khẩu / disabled ở Keycloak → 401
       • OK → đọc sub + email_verified từ access_token
            - status == SUSPENDED → 403 (thu hồi token vừa cấp)
            - email_verified && PENDING_ACTIVATION → set ACTIVE
  → trả { access_token, refresh_token, expires_in, user }

[FE] gắn Authorization: Bearer <access_token> cho mọi request
[FE] POST /auth/refresh { refreshToken }  → xin access_token mới
[FE] POST /auth/logout  { refreshToken }  → thu hồi phiên ở Keycloak
```

## B2. GIỐNG tài liệu mẫu ✅

| Bước docs | Code |
|---|---|
| 1. Nhập tài khoản/mật khẩu, gửi đi xác thực | `POST /auth/login` |
| 2. Đối chiếu mật khẩu, sai → báo lỗi | Keycloak password grant, sai → 401 |
| 3. Kiểm tra trạng thái tài khoản (bị khóa → từ chối) | Backend chặn `SUSPENDED` → 403 |
| 4. Cấp phát Token JWT trả về Client | Trả `access_token` + `refresh_token` |
| Đăng xuất (sơ đồ đăng xuất docs) | `POST /auth/logout` thu hồi refresh_token |

## B3. KHÁC tài liệu mẫu ⚠️

| | Docs | Code |
|---|---|---|
| Đường đi xác thực | Client gửi **trực tiếp sang Keycloak** | Đi **qua Backend** (proxy). Lệch câu chữ docs, nhưng cùng kết quả |
| Ai kiểm tra "trạng thái tài khoản" | Docs ghi **Keycloak** kiểm tra Statususer | Thực tế: Keycloak chỉ kiểm cờ enabled của nó; **`SUSPENDED` là trạng thái app → Backend kiểm** |
| Loại grant | Không nói rõ | Dùng **ROPC/password grant** (OAuth 2.1 khuyến cáo hạn chế, không hỗ trợ MFA) |
| Đăng nhập Google | Có mô tả (redirect, auto tạo user ACTIVE) | **Chưa làm** — ROPC không proxy được Google, phải đi Auth Code redirect riêng |

## B4. Chưa sát tài liệu / chưa làm

1. **Đăng nhập Google**: chưa làm (cần luồng Authorization Code redirect riêng).
2. **Khôi phục mật khẩu / Đổi mật khẩu** (docs có sơ đồ): chưa làm.
3. Nếu muốn đúng câu chữ "gửi trực tiếp sang Keycloak" → phải đổi sang Cách 1 (frontend dùng keycloak-js, Auth Code + PKCE), backend chỉ là resource server.

---

# PHẦN C — CÁC ENDPOINT

| Endpoint | Bảo mật | Mục đích |
|---|---|---|
| `POST /auth/register` | public | Đăng ký tài khoản mới |
| `POST /auth/login` | public | Đăng nhập (proxy Keycloak) |
| `POST /auth/refresh` | public | Cấp access_token mới từ refresh_token |
| `POST /auth/logout` | public | Thu hồi phiên tại Keycloak |
| `POST /auth/activate/{userId}` | public | Kích hoạt thủ công / webhook (PENDING → ACTIVE) |
| `GET /auth/me` | cần JWT | Lấy user hiện tại + sync ACTIVE + chặn SUSPENDED |

---

# PHẦN D — TRẠNG THÁI TÀI KHOẢN (status)

- 3 giá trị: `PENDING_ACTIVATION` → `ACTIVE` (khi verify email) / `SUSPENDED` (admin khóa).
- **Không phải** trạng thái online/offline — đây là trạng thái vòng đời tài khoản.
- Ai đổi trạng thái nào:

| Chuyển | Ai làm | Ở đâu |
|---|---|---|
| PENDING_ACTIVATION → ACTIVE | Hệ thống (sync-on-login từ email_verified) | `/auth/login`, `/auth/me` |
| ACTIVE → SUSPENDED | **Admin** (luồng riêng, CHƯA code) | — |
| SUSPENDED → ACTIVE | Admin mở khóa | — |
| Đọc SUSPENDED để chặn | Hệ thống | `/auth/login`, `/auth/me` |

> Login/`me` chỉ ĐỌC `SUSPENDED` để chặn, KHÔNG tự đặt.

---

# PHẦN E — CẤU HÌNH BẮT BUỘC (ngoài code)

## E1. Keycloak
1. **Realm riêng** cho app (KHÔNG dùng `master`); tên realm khớp `keycloak.realm` và `issuer-uri`.
2. **Realm Settings → Login → Verify email = ON**.
3. **Realm Settings → Email**: cấu hình SMTP (đang dùng Mailtrap sandbox).
4. **Client `backend-service`**:
   - Client authentication = ON, Service accounts roles = ON (cho phần đăng ký)
   - **Direct access grants = ON** (BẮT BUỘC cho `/auth/login`)
   - Tab Credentials → lấy **Client secret**
   - Tab Service accounts roles → gán `manage-users`, `view-users` (từ client `realm-management`)

## E2. application.properties
```properties
keycloak.server-url=http://192.168.0.196:8080
keycloak.realm=social-network                 # ĐỔI thành realm thật
keycloak.admin.client-id=backend-service
keycloak.admin.client-secret=CHANGE_ME_CLIENT_SECRET   # DÁN secret thật
keycloak.verify-email.redirect-uri=
keycloak.verify-email.client-id=

# Verify JWT — realm phải TỒN TẠI và Keycloak phải CHẠY, nếu không app không khởi động được
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://192.168.0.196:8080/realms/social-network
```

---

# PHẦN F — LƯU Ý BẢO MẬT & VẬN HÀNH

1. **Mọi endpoint cũ giờ cần Bearer token** (post, comment, like, message…). Chỉ `/auth/register|login|refresh|logout|activate/**` và swagger là public. Test API cũ phải kèm `Authorization: Bearer <token>`.
2. **Rollback đăng ký**: tạo user Keycloak OK nhưng lưu DB lỗi → tự `DELETE` user trên Keycloak.
3. **ROPC (login)**: tiện demo/Postman + form trong app, nhưng không hỗ trợ Google/MFA. Đúng cho đồ án, không lý tưởng cho production.
4. **decodeJwtPayload** trong login chỉ giải mã payload (không verify chữ ký) để lấy sub — chấp nhận được vì token vừa lấy trực tiếp từ Keycloak qua kênh tin cậy.

---

# PHẦN G — CÁCH TEST NHANH

## Đăng ký
1. `POST /auth/register` (body đủ trường) → 201, có mail trong Mailtrap, DB có 1 dòng PENDING_ACTIVATION.
2. Bấm link mail Mailtrap → Keycloak Email verified = ON.

## Đăng nhập
3. `POST /auth/login { username, password }` → 200 + access_token; nếu vừa verify → DB đổi ACTIVE.
4. Sai mật khẩu → 401. DB `status='SUSPENDED'` → 403.
5. `GET /auth/me` kèm Bearer token → trả thông tin user.
6. `POST /auth/refresh { refreshToken }` → token mới. `POST /auth/logout { refreshToken }` → thu hồi phiên.

---

# PHẦN H — FILE LIÊN QUAN TRONG CODE

| File | Vai trò |
|---|---|
| `Payload/Request/RegisterRequest.java` | DTO đăng ký |
| `Payload/Request/LoginRequest.java` | DTO đăng nhập |
| `Payload/Request/RefreshTokenRequest.java` | DTO refresh/logout |
| `Service/KeycloakAdminService.java` | Gọi Keycloak: token/create/verify-email/delete + login/refresh/logout |
| `Service/ServiceImpl/UserServiceImpl.java` | register / activate / getCurrentUser / login / refresh / logout |
| `Controller/AuthController.java` | /auth/register, /login, /refresh, /logout, /activate/{id}, /me |
| `Config/SecurityConfig.java` | JWT resource server + phân quyền public/authenticated |
| `Repository/UserRepository.java` | existsByEmail/Username, khóa String |
| `resources/application.properties` | Cấu hình Keycloak + issuer-uri |

---

# PHẦN I — VIỆC CÒN LẠI (TODO tổng hợp)

- [ ] Webhook kích hoạt ACTIVE (đúng docs bước 5 đăng ký)
- [ ] Đăng nhập Google (Auth Code redirect)
- [ ] Khôi phục mật khẩu / Đổi mật khẩu (docs có sơ đồ)
- [ ] Chức năng admin khóa/mở tài khoản (đặt SUSPENDED)
- [ ] Map lỗi mật khẩu yếu khi đăng ký thành 400 rõ ràng
