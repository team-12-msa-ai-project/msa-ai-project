package com.team12.common.exception;

import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException{
    private ExceptionCode exceptionCode;

    public BusinessLogicException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;

    }
}
