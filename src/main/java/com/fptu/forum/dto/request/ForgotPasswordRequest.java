package com.fptu.forum.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO dung cho chuc nang quen mat khau (ForgotPassword).
 * Tao stub de project compile - nghiep vu thuoc Auth Module (Trong).
 */
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 6, message = "Mat khau toi thieu 6 ky tu")
    private String newPassword;

    @NotBlank(message = "Xac nhan mat khau khong duoc de trong")
    private String confirmPassword;
}
