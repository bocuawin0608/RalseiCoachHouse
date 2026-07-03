package com.ralsei.exception;

import lombok.Getter;

@Getter
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
