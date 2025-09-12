package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenExpiredRetryLoginException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenExpiredRetryLoginException() {
        super(ErrorCode.TOKEN_EXPIRED_RETRY_LOGIN.getMessage());
        this.errorCode = ErrorCode.TOKEN_EXPIRED_RETRY_LOGIN;
    }
}