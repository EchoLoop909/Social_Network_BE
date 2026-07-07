package com.example.social_network.Payload.Request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Dữ liệu Client gửi lên khi đăng ký (Bước 1 - Sơ đồ hoạt động đăng ký).
 * Yêu cầu đủ thông tin để vừa tạo tài khoản Keycloak, vừa insert đầy đủ vào bảng users
 * (các cột username/email/name/firstname/lastname/surname đều NOT NULL).
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3 đến 100 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Firstname không được để trống")
    @Size(max = 100)
    private String firstname;

    @NotBlank(message = "Lastname không được để trống")
    @Size(max = 100)
    private String lastname;

    @NotBlank(message = "Surname không được để trống")
    @Size(max = 100)
    private String surname;
}
