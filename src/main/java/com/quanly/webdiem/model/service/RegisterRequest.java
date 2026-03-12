package com.quanly.webdiem.model.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]|\\\\:;\"'<>,.?/]).+$",
            message = "Mật khẩu phải có ít nhất 1 ký tự đặc biệt (vd: @)"
    )
    private String password;

    @Email(message = "Email không đúng định dạng")
    private String email; // optional
}