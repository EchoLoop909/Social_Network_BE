// Nguồn thật: Social_Network_BE/src/main/java/com/example/social_network/ResHelper/ResponseHelper.java
// Class có sẵn — KHÔNG tạo lại, chỉ gọi các method static của class này khi cần bọc envelope

package com.example.social_network.ResHelper;

import com.example.social_network.models.Dto.ResponseMess;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ResponseHelper {

    public static ResponseEntity<Object> buildFalseResponse(HttpStatus status, ResponseMess responseMess) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 1);
        map.put("Errors", responseMess);
        map.put("isOk", false);
        map.put("isError", true);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> buildResponse(Object obj, HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", "{ code:  " + 0 + ", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    // Dùng cho danh sách có phân trang — ĐÂY LÀ METHOD DÙNG CHO MỌI ENDPOINT READ/LIST
    public static ResponseEntity<Object> getResponses(Object obj, long totalElements, Integer totalPage, HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalElements", totalElements);
        map.put("totalPage", totalPage);
        map.put("Status", 0);
        map.put("Errors", "{ code:  " + 0 + ", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchMess(HttpStatus status, ResponseMess responseMess) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", responseMess);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> createRespondseFalse(HttpStatus status, ResponseMess responseMess) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 1);
        map.put("Errors", responseMess);
        map.put("isOk", false);
        map.put("isError", true);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getErrorResponse(BindingResult errors, HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", null);
        map.put("hasErrors", true);
        map.put("errors", ErrorHelper.getAllError(errors));
        map.put("timestamp", LocalDateTime.now());
        map.put("status", status.value());
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getErrorResponse(String error, HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", null);
        map.put("hasErrors", true);
        map.put("errors", error);
        map.put("timestamp", LocalDateTime.now());
        map.put("status", status.value());
        return new ResponseEntity<Object>(map, status);
    }

    // ... các method còn lại (getResponse, getResponseImport, getResponseSearchUpdate,
    // getResponseSearchInsert, getResponseSearchDelete, formatJsonResponse, v.v.)
    // giữ nguyên như file gốc trong repo — không liệt kê lại hết ở đây để tránh trùng lặp,
    // xem trực tiếp file thật khi cần dùng method khác ngoài getResponses().
}

/*
QUY TẮC SỬ DỤNG ResponseHelper (CHUẨN MỚI — đồng nhất toàn bộ):
1. Class này ĐÃ TỒN TẠI SẴN trong project — KHÔNG tạo lại, KHÔNG đổi tên, KHÔNG viết
   class envelope mới cho module khác.
2. MỌI response — kể cả CREATE/UPDATE/DELETE — đều phải đi qua ResponseHelper, không
   còn ngoại lệ:
   - READ/list → ResponseHelper.getResponses(results, totalElements, totalPages, HttpStatus.OK)
   - CREATE/UPDATE/DELETE (thành công lẫn lỗi) → 
     ResponseHelper.getResponseSearchMess(<HttpStatus>, new ResponseMess(<code>, "<message>"))
     - code = 0 khi thành công, code = 1 khi lỗi (validate/not-found/forbidden/system error)
3. TUYỆT ĐỐI KHÔNG trả ResponseEntity.ok("...")/.body("...") với string trần nữa — mọi
   endpoint phải trả cùng 1 cấu trúc { Status, Errors, isOk, isError, Object } để FE xử lý
   đồng nhất, không phải if/else theo từng loại endpoint.
4. Đây là chuẩn MỚI áp dụng cho code MỚI viết ra. Code cũ trong repo (module Comment gốc,
   JobTitle...) đang có CREATE/UPDATE/DELETE trả string thô — khi refactor lại các module
   đó, áp dụng theo chuẩn này trừ khi được yêu cầu giữ nguyên.
*/
