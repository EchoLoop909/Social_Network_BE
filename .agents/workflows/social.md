---
description: Tự động sinh bộ logic Controller, Service, Repo từ Entity có sẵn
---

Description: Đọc một Entity có sẵn và tự động sinh ra trọn bộ cấu trúc logic (Controller, Service, ServiceImpl, Repository) chuẩn mẫu và tích hợp tầng bảo mật.

## Context
- Thực thể (Entity) đã được người dùng thiết kế sẵn trong project.
- Kiến trúc áp dụng: Controller -> Service -> ServiceImpl -> Repository.
- Ràng buộc: Tuân thủ 100% các file mẫu (Template) hiện tại về cách cấu hình, import và bảo mật.

## Steps

### Bước 1: Khảo sát Entity và File mẫu
1. Hãy tìm và đọc kỹ file Entity mà người dùng chỉ định để hiểu các thuộc tính (fields) và mối quan hệ (Relationships) của nó.
2. Tìm và đọc các file mẫu hiện tại trong dự án để "học" phong cách viết code:
   - Đọc một Controller mẫu (để xem cách bọc Annotation bảo mật, endpoint mapping).
   - Đọc một Service và ServiceImpl mẫu.
   - Đọc một Repository mẫu.

### Bước 2: Tạo tầng dữ liệu (Repository)
1. Tạo file `[Entity]Repository` kế thừa từ mẫu Repository của dự án.
2. Cấu hình đúng kiểu dữ liệu của ID dựa trên file Entity đã đọc ở Bước 1.

### Bước 3: Tạo tầng Nghiệp vụ (Service & ServiceImpl)
1. Tạo interface `[Entity]Service` định nghĩa các hàm xử lý dữ liệu dựa trên các logic nghiệp vụ mà người dùng yêu cầu.
2. Tạo class `[Entity]ServiceImpl` để triển khai (implement) interface đó.
3. Inject `[Entity]Repository` vào và viết các logic kiểm tra dữ liệu (Validation), xử lý ràng buộc nghiệp vụ theo đúng mô tả của người dùng.

### Bước 4: Tạo tầng Điều hướng & Bảo mật (Controller)
1. Tạo class `[Entity]Controller`.
2. Áp dụng chính xác cơ chế bảo mật từ file mẫu (như check Token, phân quyền `@PreAuthorize`, hoặc các quy tắc kiểm tra quyền sở hữu dữ liệu - Owner Check) vào các endpoint mới tạo.
3. Đảm bảo cấu trúc dữ liệu trả về (Response Body) và các HTTP Status Code đồng bộ với chuẩn chung của dự án.

### Bước 5: Hoàn tất và Báo cáo
1. Rà soát lại toàn bộ mã nguồn vừa sinh ra để đảm bảo không bị lỗi syntax, compile hoặc thiếu import.
2. Liệt kê danh sách các file logic vừa được tạo mới cho người dùng.