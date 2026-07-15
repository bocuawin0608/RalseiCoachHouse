package com.ralsei.exception;

/**
 * Signals a resource not found error condition.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
}
