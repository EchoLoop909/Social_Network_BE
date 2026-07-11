// Nguồn thật: Social_Network_BE/src/main/java/com/example/social_network/Service/CommentService.java
// Dùng làm mẫu convention cho MỌI Service interface mới trong package Service

package com.example.social_network.Service;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;

public interface CommentService {
    ResponseEntity<?> addComment(CommentRequest dto, String ip);

    Object getListByPost(String postId, int pageIdx, int pageSize);

    ResponseEntity<?> updateComment(CommentUpdateDto dto, String ip);

    ResponseEntity<?> deleteComment(DeleteDto dto, String ip);
}

/*
QUY TẮC BẮT BUỘC khi tạo Service interface mới:
1. package com.example.social_network.Service
2. Đặt tên method theo dạng độngTừ + DanhTừ, camelCase: addComment, getListByPost,
   updateComment, deleteComment — KHÔNG dùng snake_case (get_list, insert_data là
   convention của project KHÁC, không áp dụng ở đây)
3. KIỂU TRẢ VỀ theo đúng phân loại thao tác (xem quy tắc response ở response_helper file):
   - Method CREATE/UPDATE/DELETE → ResponseEntity<?>
   - Method READ (list/pagination) → Object (vì có lúc trả Page bọc envelope, có lúc
     trả thẳng ResponseEntity lỗi validate — kiểu Object linh hoạt cho cả hai)
4. Method ghi dữ liệu (create/update/delete) LUÔN nhận thêm tham số String ip cuối cùng
   — dùng để log audit / IP truy vấn, ĐÚNG convention project
5. DTO request đặt tên theo dạng <Entity><Action>Dto (CommentUpdateDto) hoặc
   <Entity>Request (CommentRequest) cho create — theo đúng những gì đã thấy trong module Comment
6. Dùng chung DeleteDto (com.example.social_network.models.Dto.DeleteDto) cho MỌI
   thao tác xoá theo id — KHÔNG tạo DTO xoá riêng cho từng entity
*/
