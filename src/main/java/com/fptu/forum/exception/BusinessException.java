package com.fptu.forum.exception;

/**
 * Exception danh cho vi pham business rule (khong phai loi ky thuat).
 * Ví du: ban chinh minh, promote user bi BANNED...
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
