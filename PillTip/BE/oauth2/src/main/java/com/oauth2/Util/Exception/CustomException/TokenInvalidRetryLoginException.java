package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenInvalidRetryLoginException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenInvalidRetryLoginException() {
        super(ErrorCode.TOKEN_INVALID_RETRY_LOGIN.getMessage());
        this.errorCode = ErrorCode.TOKEN_INVALID_RETRY_LOGIN;
    }
}