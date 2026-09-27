package com.bodha.exception;

/**
 * Thrown when an account registration attempt uses an already registered email.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
