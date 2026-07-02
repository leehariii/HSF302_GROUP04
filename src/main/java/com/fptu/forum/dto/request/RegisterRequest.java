package com.fptu.forum.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu form dang ky tai khoan.
 * Dung Bean Validation de kiem tra dau vao.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username khong duoc de trong")
    @Size(min = 3, max = 50, message = "Username phai tu 3 den 50 ky tu")
    private String username;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, max = 100, message = "Mat khau phai tu 6 den 100 ky tu")
    private String password;

    @NotBlank(message = "Xac nhan mat khau khong duoc de trong")
    private String confirmPassword;

    @Size(max = 100, message = "Ho ten toi da 100 ky tu")
    private String fullName;
}
