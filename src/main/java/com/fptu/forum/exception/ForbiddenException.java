package com.fptu.forum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Nem ra khi nguoi dung khong co quyen thuc hien hanh dong.
 * Vi du: sua bai cua nguoi khac, dang bai khi bi MUTE...
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
