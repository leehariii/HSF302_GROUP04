package com.fptu.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu form bao cao vi pham.
 */
@Getter
@Setter
public class ReportRequest {

    @NotBlank(message = "Ly do bao cao khong duoc de trong")
    @Size(max = 500, message = "Ly do toi da 500 ky tu")
    private String reason;

    // Mot trong hai phai co gia tri (kiem tra o service)
    private Long postId;
    private Long commentId;
}
