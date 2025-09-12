package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST.getMessage());
        this.errorCode = ErrorCode.INVALID_REQUEST;
    }
}