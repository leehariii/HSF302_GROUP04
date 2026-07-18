package com.fptu.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Form nhan ly do ban user tu Admin.
 */
@Getter
@Setter
public class BanUserForm {

    @NotBlank(message = "Lý do khóa tài khoản không được để trống")
    @Size(max = 255, message = "Lý do khóa không được vượt quá 255 ký tự")
    private String reason;
}
