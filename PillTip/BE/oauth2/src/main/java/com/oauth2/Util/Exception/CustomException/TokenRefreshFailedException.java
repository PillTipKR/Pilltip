package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class TokenRefreshFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public TokenRefreshFailedException() {
        super(ErrorCode.TOKEN_REFRESH_FAILED.getMessage());
        this.errorCode = ErrorCode.TOKEN_REFRESH_FAILED;
    }
}