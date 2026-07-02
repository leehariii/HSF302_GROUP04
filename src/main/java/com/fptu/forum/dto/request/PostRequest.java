package com.fptu.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu form tao/sua bai viet.
 */
@Getter
@Setter
public class PostRequest {

    @NotBlank(message = "Tieu de khong duoc de trong")
    @Size(max = 255, message = "Tieu de toi da 255 ky tu")
    private String title;

    @NotBlank(message = "Noi dung khong duoc de trong")
    private String content;

    @NotNull(message = "Vui long chon topic")
    private Long topicId;

    @jakarta.validation.constraints.Pattern(regexp = "^(https?://.*)?$", message = "URL ảnh phải bắt đầu bằng http:// hoặc https://")
    @Size(max = 2000, message = "URL ảnh quá dài")
    private String imageUrl;
}
