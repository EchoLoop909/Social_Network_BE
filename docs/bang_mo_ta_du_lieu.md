# BẢNG MÔ TẢ DỮ LIỆU (DATA DICTIONARY)

Hệ thống gồm **17 bảng**, ánh xạ 1-1 từ các lớp Entity trong mã nguồn Backend. Toàn bộ khóa chính dùng chuỗi `VARCHAR(36)` (UUID). Riêng bảng `users`, khóa chính `id_user` đồng thời là định danh `sub` do Keycloak cấp (không dùng cột `keycloakId` riêng).

> Quy ước cột: **PK** = Khóa chính, **FK** = Khóa ngoại, **UQ** = Ràng buộc duy nhất, **NN** = Không null (NOT NULL).

---

## Nhóm 1 — Người dùng và quan hệ mạng lưới

### Bảng 1. `users` — Người dùng

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_user | VARCHAR(36) | PK, NN | Khóa chính; trùng với `sub` (UUID) do Keycloak cấp |
| 2 | username | VARCHAR(100) | UQ, NN | Tên đăng nhập, duy nhất |
| 3 | email | VARCHAR(100) | UQ, NN | Email, duy nhất |
| 4 | name | VARCHAR(100) | NN | Tên hiển thị |
| 5 | firstname | VARCHAR(100) | NN | Tên |
| 6 | lastname | VARCHAR(100) | NN | Họ |
| 7 | surname | VARCHAR(100) | NN | Tên đệm / họ phụ |
| 8 | photo | VARCHAR(255) | | URL ảnh đại diện |
| 9 | description | VARCHAR(2000) | | Tiểu sử / mô tả hồ sơ |
| 10 | creation_date | DATETIME | | Thời điểm tạo tài khoản |
| 11 | deletion_date | DATETIME | | Thời điểm xóa (soft delete) |
| 12 | is_checked | BIT(1) | | Cờ đã xác minh |
| 13 | is_private | BIT(1) | NN | true = tài khoản riêng tư, false = công khai |
| 14 | status | VARCHAR(20) | NN | Trạng thái: PENDING_ACTIVATION / ACTIVE / SUSPENDED |

### Bảng 2. `followerships` — Quan hệ theo dõi (User ↔ User, M:N)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_followership | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_user_checked | VARCHAR(36) | FK→users, NN | Người được theo dõi |
| 3 | id_user_follower | VARCHAR(36) | FK→users, NN | Người đi theo dõi |
| 4 | status | VARCHAR(10) | NN | FOLLOWING (đang theo dõi – set ngay khi gửi lời mời) / ACCEPTED (đã được chấp nhận – bạn bè, xem được cả tài khoản riêng tư) / PENDING (dữ liệu cũ, chờ duyệt) / REJECTED (đã bị từ chối) / BLOCKED (đã chặn; khi đó id_user_follower là người chặn, id_user_checked là người bị chặn) |
| 5 | create_time | DATETIME | NN | Thời điểm tạo |
| 6 | update_time | DATETIME | | Thời điểm cập nhật trạng thái gần nhất (null nếu chưa từng đổi) |
| 7 | accepted_at | DATETIME | | Thời điểm được chấp nhận (chuyển sang ACCEPTED); null nếu chưa được duyệt |

> **UQ** (id_user_checked, id_user_follower): không theo dõi trùng.
> Quan hệ BLOCKED dùng chung bảng này: một bản ghi BLOCKED thay thế mọi quan hệ theo dõi/kết bạn cũ giữa hai người; hai bên bị ẩn hoàn toàn với nhau (feed, tìm kiếm, trang cá nhân, lời mời).

---

## Nhóm 2 — Bài viết và nội dung

### Bảng 3. `posts` — Bài viết

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_post | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | text | VARCHAR(5000) | | Nội dung / caption |
| 3 | post_type | VARCHAR(20) | NN | IMAGE / VIDEO / CAROUSEL / TEXT |
| 4 | visibility | VARCHAR(20) | NN | PUBLIC / FOLLOWERS / PRIVATE |
| 5 | status | VARCHAR(20) | NN | Kiểm duyệt: PENDING_REVIEW / PUBLISHED / FLAGGED |
| 6 | create_time | DATETIME | NN | Thời điểm đăng |
| 7 | is_pinned | BIT(1) | | Ghim bài lên đầu hồ sơ |
| 8 | reaction_count | INT | NN | Bộ đếm cảm xúc (denormalized) |
| 9 | comment_count | INT | NN | Bộ đếm bình luận (denormalized) |
| 10 | id_original_post | VARCHAR(36) | FK→posts | Bài gốc được share trực tiếp; null nếu là bài gốc hoặc share mà gốc đã bị xóa. **ON DELETE SET NULL** |
| 11 | share_count | INT | NN | Bộ đếm số lần bài này bị share (denormalized) |
| 12 | is_shared | BIT(1) | NN | true = bài này là 1 lượt share; set khi tạo và không đổi lại kể cả khi id_original_post về null |
| 13 | id_user | VARCHAR(36) | FK→users, NN | Tác giả bài viết |

> **FK tự tham chiếu** `id_original_post → posts.id_post` với **ON DELETE SET NULL**: khi bài gốc bị xóa, các bài share trỏ tới nó tự động về null (không xóa dây chuyền), còn `is_shared` giữ nguyên true để phân biệt "bài gốc thật" (is_shared=false) với "bài share mà gốc đã mất" (is_shared=true, id_original_post=null).

### Bảng 4. `post_media` — Media của bài viết

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_media | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_post | VARCHAR(36) | FK→posts, NN | Bài viết chứa media |
| 3 | media_url | VARCHAR(2048) | NN | Đường dẫn ảnh/video |
| 4 | media_type | VARCHAR(10) | NN | IMAGE / VIDEO |
| 5 | display_order | INT | NN | Thứ tự hiển thị trong carousel (0-indexed) |
| 6 | width | INT | | Chiều rộng (px) |
| 7 | height | INT | | Chiều cao (px) |
| 8 | duration_sec | INT | | Thời lượng video (giây); null nếu là ảnh |

> **UQ** (id_post, display_order): không trùng thứ tự trong một bài.

### Bảng 5. `comments` — Bình luận

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_comment | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | text | VARCHAR(2000) | NN | Nội dung bình luận |
| 3 | create_time | DATETIME | NN | Thời điểm bình luận |
| 4 | id_user | VARCHAR(36) | FK→users, NN | Người bình luận |
| 5 | id_post | VARCHAR(36) | FK→posts, NN | Bài viết được bình luận |
| 6 | id_parent_comment | VARCHAR(36) | FK→comments | Bình luận cha (null nếu gốc) — tạo cây phân cấp |
| 7 | reaction_count | INT | NN | Bộ đếm cảm xúc của bình luận (denormalized) |

### Bảng 6. `hashtags` — Thẻ từ khóa

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_hashtag | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | name | VARCHAR(100) | UQ, NN | Tên thẻ (không chứa dấu #) |
| 3 | post_count | INT | NN | Số bài dùng thẻ (denormalized) |
| 4 | created_at | DATETIME | NN | Thời điểm tạo |

### Bảng 7. `post_hashtags` — Nối Post ↔ Hashtag (M:N thuần)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_post | VARCHAR(36) | PK (tổ hợp), FK→posts | Bài viết |
| 2 | id_hashtag | VARCHAR(36) | PK (tổ hợp), FK→hashtags | Thẻ từ khóa |

> Khóa chính tổ hợp (id_post, id_hashtag), không có thuộc tính riêng.

### Bảng 8. `post_user_tags` — Gắn thẻ người dùng vào bài (M:N có thuộc tính)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_post | VARCHAR(36) | PK (tổ hợp), FK→posts | Bài viết |
| 2 | id_user | VARCHAR(36) | PK (tổ hợp), FK→users | Người được tag |
| 3 | x_position | DECIMAL(5,2) | | Tọa độ X (%) trên ảnh; null nếu tag trong caption |
| 4 | y_position | DECIMAL(5,2) | | Tọa độ Y (%) trên ảnh |

---

## Nhóm 3 — Tương tác

### Bảng 9. `likes` — Cảm xúc (đa hình)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_like | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | target_type | VARCHAR(10) | NN | POST / COMMENT (đa hình) |
| 3 | target_id | VARCHAR(36) | NN | ID đối tượng được thả cảm xúc (không FK cứng) |
| 4 | id_user | VARCHAR(36) | FK→users, NN | Người thả cảm xúc |
| 5 | reaction_type | VARCHAR(10) | NN | LIKE / HEART / HAHA / WOW / SAD / ANGRY |
| 6 | create_time | DATETIME | NN | Thời điểm |

> **UQ** (id_user, target_type, target_id): mỗi user chỉ một cảm xúc trên một đối tượng.

---

## Nhóm 4 — Stories (tin 24 giờ)

### Bảng 10. `stories` — Tin

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_story | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_user | VARCHAR(36) | FK→users, NN | Chủ tin |
| 3 | media_url | VARCHAR(2048) | NN | Ảnh/video của tin |
| 4 | media_type | VARCHAR(10) | NN | IMAGE / VIDEO |
| 5 | caption | VARCHAR(2200) | | Chú thích |
| 6 | created_at | DATETIME | NN | Thời điểm tạo |
| 7 | expires_at | DATETIME | NN | Hết hạn = created_at + 24h |
| 8 | is_archived | BIT(1) | NN | true = đã lưu Highlights (không tự xóa) |
| 9 | metadata | JSON | | Sticker, nhạc, text overlay… |

### Bảng 11. `story_views` — Lượt xem tin (M:N thuần)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_story | VARCHAR(36) | PK (tổ hợp), FK→stories | Tin được xem |
| 2 | id_user | VARCHAR(36) | PK (tổ hợp), FK→users | Người xem |
| 3 | viewed_at | DATETIME | NN | Thời điểm xem |

> Khóa chính tổ hợp (id_story, id_user): mỗi user tính một lần/tin.

---

## Nhóm 5 — Nhắn tin

### Bảng 12. `conversations` — Hội thoại

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_conversation | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | name | VARCHAR(255) | | Tên nhóm (null nếu chat 1-1) |
| 3 | type | VARCHAR(10) | NN | SINGLE (1-1) / GROUP |
| 4 | create_time | DATETIME | NN | Thời điểm tạo |
| 5 | last_message_time | DATETIME | | Thời điểm tin mới nhất (sắp xếp Inbox) |

### Bảng 13. `participants` — Thành viên hội thoại (M:N có thuộc tính)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_participant | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_conversation | VARCHAR(36) | FK→conversations, NN | Hội thoại |
| 3 | id_user | VARCHAR(36) | FK→users, NN | Thành viên |
| 4 | last_read_message_id | VARCHAR(36) | | Tin cuối user đã đọc (đánh dấu đã đọc/chưa đọc) |
| 5 | joined_time | DATETIME | NN | Thời điểm tham gia |

### Bảng 14. `messages` — Tin nhắn

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_message | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_conversation | VARCHAR(36) | FK→conversations, NN | Hội thoại chứa tin |
| 3 | id_user_transmitter | VARCHAR(36) | FK→users, NN | Người gửi |
| 4 | text | VARCHAR(3000) | | Nội dung (null nếu chỉ gửi ảnh) |
| 5 | photo | VARCHAR(255) | | Ảnh đính kèm |
| 6 | id_shared_post | VARCHAR(36) | FK→posts | Bài viết chia sẻ (khi message_type = SHARED_POST) |
| 7 | message_type | VARCHAR(15) | NN | TEXT / IMAGE / VIDEO / SHARED_POST / SYSTEM |
| 8 | create_time | DATETIME | NN | Thời điểm gửi |

---

## Nhóm 6 — Thông báo và kiểm duyệt

### Bảng 15. `notifications` — Thông báo

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_notification | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_recipient | VARCHAR(36) | FK→users, NN | Người nhận thông báo |
| 3 | id_actor | VARCHAR(36) | FK→users | Người gây sự kiện (null nếu hệ thống) |
| 4 | type | VARCHAR(20) | NN | Loại: like/comment/reply/follow/tag… |
| 5 | target_id | VARCHAR(36) | | ID đối tượng liên quan (điều hướng khi bấm) |
| 6 | is_read | BIT(1) | NN | Đã đọc hay chưa |
| 7 | create_time | DATETIME | NN | Thời điểm |

### Bảng 16. `reports` — Báo cáo vi phạm (đa hình)

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_report | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | id_reporter | VARCHAR(36) | FK→users, NN | Người gửi báo cáo |
| 3 | target_type | VARCHAR(10) | NN | POST / USER (đa hình) |
| 4 | target_id | VARCHAR(36) | NN | ID đối tượng bị báo cáo (không FK cứng) |
| 5 | id_report_reason | VARCHAR(36) | FK→report_reasons, NN | Lý do báo cáo |
| 6 | extra_information | VARCHAR(2000) | | Thông tin bổ sung |
| 7 | status | VARCHAR(10) | NN | PENDING / RESOLVED / REJECTED |
| 8 | create_time | DATETIME | NN | Thời điểm |

### Bảng 17. `report_reasons` — Danh mục lý do báo cáo

| STT | Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|----|------------|--------------|-----------|-------|
| 1 | id_report_reason | VARCHAR(36) | PK, NN | Khóa chính |
| 2 | reason | VARCHAR(500) | NN | Nội dung lý do (spam, quấy rối, nhạy cảm…) |
