package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidTokenException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN.getMessage());
        this.errorCode = ErrorCode.INVALID_TOKEN;
    }
}