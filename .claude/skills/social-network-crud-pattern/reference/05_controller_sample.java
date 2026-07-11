// Nguồn thật: Social_Network_BE/src/main/java/com/example/social_network/Controller/CommentController.java
// Dùng làm mẫu convention cho MỌI Controller mới trong package Controller

package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.CommentService;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.COMMENT)
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> addComment(@RequestBody CommentRequest dto,
                                        HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return commentService.addComment(dto, ip);
    }

    // READ
    @GetMapping(PathResources.GETCOMMENT)
    public Object getCommentsByPost(
            @RequestParam String postId,
            @RequestParam(defaultValue = "1") int pageIdx,
            @RequestParam(defaultValue = "10") int pageSize) {
        return commentService.getListByPost(postId, pageIdx - 1, pageSize);
    }

    @PutMapping(PathResources.UPDATE)
    public ResponseEntity<?> updateComment(@RequestBody CommentUpdateDto dto, HttpServletRequest request) {
        return commentService.updateComment(dto, request.getRemoteAddr());
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> deleteComment(@RequestBody DeleteDto dto, HttpServletRequest request) {
        return commentService.deleteComment(dto, request.getRemoteAddr());
    }
}

/*
QUY TẮC BẮT BUỘC khi tạo Controller mới:
1. package com.example.social_network.Controller (chữ C hoa)
2. @RestController + @RequestMapping(PathResources.<TEN_MODULE_VIET_HOA>)
   — route KHÔNG hardcode string, luôn lấy hằng số từ
   com.example.social_network.Payload.Util.PathResources. Nếu path chưa tồn tại,
   phải thêm hằng số mới vào PathResources trước, không tự bịa string route.
3. Method mapping dùng đúng HTTP verb theo thao tác:
   - @PostMapping cho create (PathResources.INSERT)
   - @GetMapping cho read (PathResources.GET<MODULE>)
   - @PutMapping cho update (PathResources.UPDATE)
   - @DeleteMapping cho delete (PathResources.DELETE)
4. Controller CHỈ pass-through — không chứa logic nghiệp vụ, không tự validate,
   không tự bọc response. Chỉ gọi service và return thẳng kết quả.
5. Lấy IP từ HttpServletRequest request.getRemoteAddr() truyền xuống Service cho
   mọi thao tác ghi (create/update/delete)
6. Kiểu trả về method Controller PHẢI khớp kiểu trả về của Service tương ứng:
   ResponseEntity<?> cho create/update/delete, Object cho read theo trang
7. Tham số phân trang: pageIdx nhận từ FE bắt đầu từ 1, trừ đi 1 khi truyền
   xuống Service (Service dùng pageIdx 0-based của Spring Data)
*/
