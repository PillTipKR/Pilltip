package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenExpiredDetailException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenExpiredDetailException() {
        super(ErrorCode.TOKEN_EXPIRED_DETAIL.getMessage());
        this.errorCode = ErrorCode.TOKEN_EXPIRED_DETAIL;
    }
}