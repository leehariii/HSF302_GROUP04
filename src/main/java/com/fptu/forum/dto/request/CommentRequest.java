package com.fptu.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu form binh luan / reply comment.
 */
@Getter
@Setter
public class CommentRequest {

    @NotBlank(message = "Noi dung binh luan khong duoc de trong")
    private String content;

    // null neu la comment goc, co gia tri neu la reply
    private Long parentCommentId;
}
