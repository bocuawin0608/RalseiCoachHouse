package com.ralsei.exception;

import lombok.Getter;

@Getter
/**
 * Signals a business rule error condition.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;
    private final Object details;

    public BusinessRuleException(String message) {
        super(message);
        this.code = null;
        this.details = null;
    }

    public BusinessRuleException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }
}
