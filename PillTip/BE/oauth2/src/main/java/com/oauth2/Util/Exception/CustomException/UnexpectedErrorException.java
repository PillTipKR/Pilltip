package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UnexpectedErrorException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UnexpectedErrorException() {
        super(ErrorCode.UNEXPECTED_ERROR.getMessage());
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }
}