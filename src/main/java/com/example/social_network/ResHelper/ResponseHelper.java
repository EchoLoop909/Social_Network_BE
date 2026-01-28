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

    public static ResponseEntity<Object> buildFalseResponse (HttpStatus status, ResponseMess responseMess) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 1);
        map.put("Errors", responseMess);
        map.put("isOk", false);
        map.put("isError", true);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);

    }

    public static ResponseEntity<Object> buildResponse(Object obj,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getErrorResponse(Object obj,Integer totalPage,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponse(Object obj,Integer totalPage,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponses(Object obj, long totalElements, Integer totalPage, HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalElements",totalElements);
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }
    public static ResponseEntity<Object> getResponseImport(Object obj,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseImport(Object obj,HttpStatus status, String mess) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0 );
        map.put("Message:", mess);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchUpdate(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchInsert(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", "SUCCESS");
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

    public static ResponseEntity<Object> getResponseSearchInsertJobTitles(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchDelete(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", "{ Code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
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

    public static ResponseEntity<String> formatJsonResponse(ResponseEntity<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(response.getBody(), Object.class);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String prettyJson = writer.writeValueAsString(json);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(prettyJson, headers, response.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>("{\"error\":\"Error processing JSON response\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    public static ResponseEntity<String> formatJsonErrorResponse(RestClientResponseException e) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            Object json = mapper.readValue(e.getResponseBodyAsString(), Object.class);
//            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//            String prettyJson = writer.writeValueAsString(json);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            return new ResponseEntity<>(prettyJson, headers, e.getRawStatusCode());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            return new ResponseEntity<>("{\"error\":\"Error processing JSON error response\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}




/*

package bkav.com.springboot.ResHelper;

        import bkav.com.springboot.models.Dto.ResponseMess;
        import bkav.com.springboot.services.PermissionService;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.http.HttpHeaders;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.validation.BindingResult;

        import java.time.LocalDateTime;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.Set;

public class ResponseHelper {
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
    public static ResponseEntity<Object> getErrorResponse(Object obj,Integer totalPage,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponse(Object obj,Integer totalPage,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchUpdate(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchInsert(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", "SUCCESS");
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

    public static ResponseEntity<Object> getResponseSearchInsertJobTitles(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", null);
        return new ResponseEntity<Object>(map, status);
    }

    public static ResponseEntity<Object> getResponseSearchDelete(HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", "{ Code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
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
    //Tiến anh
    public static ResponseEntity<Object> getResponseImport(Object obj,HttpStatus status) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);
        return new ResponseEntity<Object>(map, status);
    }



    */
/**        Xu ly Header            **//*

    public static ResponseEntity<Object> getResponseAndAddHeader(Object obj,Integer totalPage,HttpStatus status, Map<String, String> headers) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalPage",totalPage);
        map.put("Status", 0 );
        map.put("Errors", "{ code:  " + 0 +", message: SUCCESS }");
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", obj);


        */
/** xu ly phan add header **//*

        HttpHeaders responseHeaders = new HttpHeaders();
        Set<String> headerNames = headers.keySet();
        logger.info("Header:" + headerNames);
        for(String name: headerNames) {
            responseHeaders.set(name, headers.get(name));
            logger.info("Name: " + name);
            logger.info("Header Value: " + headers.get(name));
        }
        return new ResponseEntity<Object>(map, responseHeaders,status);
    }
    public static ResponseEntity<?> getResponseSearchInsertAndAddHeader(HttpStatus status, Map<String, String> headers) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Status", 0);
        map.put("Errors", null);
        map.put("isOk", true);
        map.put("isError", false);
        map.put("Object", "SUCCESS");

        */
/** xu ly phan add header **//*

        HttpHeaders responseHeaders = new HttpHeaders();
        Set<String> headerNames = headers.keySet();
        logger.info("Header:" + headerNames);
        for(String name: headerNames) {
            responseHeaders.set(name, headers.get(name));
            logger.info("Name: " + name);
            logger.info("Header Value: " + headers.get(name));
        }
        return new ResponseEntity<Object>(map, responseHeaders,status);

    }

}*/
