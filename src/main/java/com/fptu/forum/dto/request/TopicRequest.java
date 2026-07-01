package com.fptu.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu form tao/sua topic (Admin).
 */
@Getter
@Setter
public class TopicRequest {

    @NotBlank(message = "Ten topic khong duoc de trong")
    @Size(max = 100, message = "Ten topic toi da 100 ky tu")
    private String name;

    @Size(max = 500, message = "Mo ta toi da 500 ky tu")
    private String description;
}
