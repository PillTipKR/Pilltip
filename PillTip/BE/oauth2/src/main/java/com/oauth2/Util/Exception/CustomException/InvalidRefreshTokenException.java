package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        this.errorCode = ErrorCode.INVALID_REFRESH_TOKEN;
    }
}