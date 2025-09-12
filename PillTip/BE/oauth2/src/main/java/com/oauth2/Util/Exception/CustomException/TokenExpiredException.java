package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenExpiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED.getMessage());
        this.errorCode = ErrorCode.TOKEN_EXPIRED;
    }
}