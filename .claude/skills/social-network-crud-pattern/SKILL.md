---
name: social-network-crud-pattern
description: Dùng khi tạo Entity, Repository, Service, Controller, hoặc bất kỳ module CRUD mới nào cho backend SocialNetwork (package com.example.social_network, Spring Boot 2.x, javax.*). Trigger khi user yêu cầu "tạo API cho...", "thêm entity...", "sinh CRUD cho bảng/module...", "thêm chức năng like/share/report...", hoặc bất kỳ yêu cầu nào cần viết code theo đúng convention hiện có của repo SocialNetwork_BE.
---

# SocialNetwork CRUD Pattern

Skill này encode convention thực tế (không phải lý thuyết) của module **Comment** —
module CRUD đầy đủ và tiêu biểu nhất trong repo, được dùng làm chuẩn tham chiếu cho
mọi module mới.

## Quy trình bắt buộc khi được yêu cầu tạo entity/API mới

1. Đọc `reference/01_entity_sample.java` — convention Entity
2. Đọc `reference/02_repository_sample.java` — convention Repository
3. Đọc `reference/03_service_interface_sample.java` — convention Service interface
4. Đọc `reference/04_service_impl_sample.java` — convention ServiceImpl (validate, try/catch,
   logging, response theo từng loại thao tác)
5. Đọc `reference/05_controller_sample.java` — convention Controller
6. Đọc `reference/06_response_helper.java` — cách dùng `ResponseHelper` có sẵn, KHÔNG tạo mới
7. Làm theo đúng thứ tự trong `checklist.md`, không bỏ bước, không đổi thứ tự

## Quy tắc bắt buộc (tổng hợp — chi tiết xem trong từng file reference)

- Package: `Controller` (C hoa), `Service`, `Service/ServiceImpl`, `Repository` (R hoa),
  `models/Entity`, `models/Dto`, `Payload/Request`, `Payload/Util` — giữ nguyên casing này,
  không tự chuẩn hoá thành lowercase.
- Spring Boot 2.x → import `javax.persistence.*`, `javax.servlet.*` — **KHÔNG** dùng `jakarta.*`.
- Entity: Lombok `@Data`, `@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})`,
  id String UUID tự sinh trong `@PrePersist`, quan hệ `@ManyToOne` luôn `LAZY`, quan hệ
  ngược `@OneToMany` luôn `@JsonIgnore`.
- Repository: derived query method ưu tiên hơn `@Query` native khi có thể.
- Service/ServiceImpl: validate thủ công bằng if/else (không dùng `@Valid`/Bean Validation),
  try/catch toàn bộ thân method, `@Autowired` field injection.
- **Response — quy tắc quan trọng nhất, dễ làm sai nhất — BẮT BUỘC ĐỒNG NHẤT:**
  - **MỌI** endpoint (READ lẫn CREATE/UPDATE/DELETE) đều phải bọc qua `ResponseHelper`,
    không có ngoại lệ nào — để FE luôn nhận đúng 1 cấu trúc response duy nhất.
  - READ/list → `ResponseHelper.getResponses(results, totalElements, totalPages, HttpStatus.OK)`
  - CREATE/UPDATE/DELETE thành công → 
    `ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "<VERB> <NOUN> SUCCESS"))`
  - Lỗi validate / not-found / forbidden / system error → 
    `ResponseHelper.getResponseSearchMess(<HttpStatus tương ứng>, new ResponseMess(1, "<message>"))`
  - **TUYỆT ĐỐI KHÔNG** trả `ResponseEntity.ok("...")` hay `.body("...")` với string trần —
    dù là code cũ trong repo (module Comment gốc, JobTitle...) đang làm vậy. Đây là chuẩn
    MỚI được người dùng chốt, ưu tiên hơn code cũ và hơn cả mô tả trong CLAUDE.md.
  - Khi refactor module CŨ, áp dụng lại theo chuẩn đồng nhất này trừ khi người dùng nói
    rõ giữ nguyên.
- Controller: chỉ pass-through, route lấy từ `PathResources`, không hardcode string route,
  không chứa logic nghiệp vụ.
- Thao tác xoá dùng chung `DeleteDto` có sẵn, không tạo DTO xoá riêng cho từng entity.

## Khi mẫu không cover được trường hợp

Nếu yêu cầu có tình huống không khớp với bất kỳ pattern nào trong các file reference
(ví dụ: quan hệ nhiều-nhiều, soft delete, upload file kèm theo, real-time qua WebSocket),
**hỏi lại người dùng** cách xử lý mong muốn thay vì tự sáng tạo pattern mới. Ưu tiên tìm
module tương tự khác trong repo (ví dụ Like, Post) để tham chiếu thêm nếu cần, thay vì suy
đoán.
