package com.fptu.forum.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO dung cho chuc nang cap nhat profile nguoi dung.
 * Tao stub de project compile - nghiep vu thuoc Auth/Profile Module.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100, message = "Ho ten toi da 100 ky tu")
    private String fullName;

    @Size(max = 255, message = "URL avatar toi da 255 ky tu")
    private String avatarUrl;
}
