package com.fptu.forum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Nem ra khi khong tim thay tai nguyen (post, user, topic...).
 * Spring MVC tu dong tra ve 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " voi id=" + id + " khong ton tai.");
    }
}
