package com.bodha.exception;

/**
 * Thrown when attempting to create a goal that conflicts with an existing goal for the same user and subject.
 */
public class DuplicateGoalException extends RuntimeException {
    public DuplicateGoalException(String message) {
        super(message);
    }
}
