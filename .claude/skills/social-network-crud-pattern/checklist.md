# Checklist tạo 1 module CRUD mới (theo pattern module Comment)

Làm theo đúng thứ tự, không bỏ bước:

1. **Entity** (`models/Entity/<Ten>.java`)
   - Copy cấu trúc từ `reference/01_entity_sample.java`
   - Xác định field, quan hệ (@ManyToOne/@OneToMany) dựa trên yêu cầu
   - Thêm @PrePersist sinh id + createTime

2. **Repository** (`Repository/<Ten>Repository.java`)
   - extends JpaRepository<Entity, String>
   - Thêm derived query method nếu cần lọc/sort theo FK

3. **DTO** (`models/Dto/` hoặc `Payload/Request/`)
   - DTO tạo mới: `<Ten>Request` (theo mẫu CommentRequest)
   - DTO cập nhật: `<Ten>UpdateDto`
   - DÙNG LẠI `DeleteDto` có sẵn cho thao tác xoá — không tạo DTO xoá riêng

4. **PathResources** (`Payload/Util/PathResources.java`)
   - Kiểm tra xem hằng số route cho module này đã tồn tại chưa
   - Nếu chưa, thêm các hằng số cần thiết (route module + INSERT/UPDATE/DELETE/GET<MODULE>)
     theo đúng convention đặt tên đã có trong file

5. **Service interface** (`Service/<Ten>Service.java`)
   - Copy cấu trúc từ `reference/03_service_interface_sample.java`
   - method create/update/delete trả ResponseEntity<?>, nhận thêm tham số `String ip`
   - method list trả Object

6. **ServiceImpl** (`Service/ServiceImpl/<Ten>ServiceImpl.java`)
   - Copy cấu trúc từ `reference/04_service_impl_sample.java`
   - Validate input thủ công bằng if/else, return sớm khi lỗi
   - try/catch toàn bộ thân method + logger.error dùng placeholder {}
   - **MỌI response (READ lẫn CREATE/UPDATE/DELETE) đều bọc `ResponseHelper`**:
     READ dùng `getResponses(...)`, còn lại dùng
     `getResponseSearchMess(status, new ResponseMess(code, message))`
     (code 0 = thành công, code 1 = lỗi) — không trả string trần

7. **Controller** (`Controller/<Ten>Controller.java`)
   - Copy cấu trúc từ `reference/05_controller_sample.java`
   - @RequestMapping(PathResources.<MODULE>), method mapping đúng verb HTTP
   - Controller chỉ pass-through, lấy IP từ HttpServletRequest, gọi service, return thẳng

8. **Kiểm tra chéo cuối cùng**
   - So khớp kiểu trả về giữa Controller ↔ Service interface ↔ ServiceImpl
   - Đảm bảo route trong PathResources không trùng với module khác
   - Đảm bảo package đúng: Controller (chữ C hoa), Service, Service/ServiceImpl, Repository
     (chữ R hoa), models/Entity, models/Dto (chữ thường "models")
