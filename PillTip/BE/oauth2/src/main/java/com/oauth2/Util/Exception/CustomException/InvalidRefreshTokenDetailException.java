package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRefreshTokenDetailException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidRefreshTokenDetailException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN_DETAIL.getMessage());
        this.errorCode = ErrorCode.INVALID_REFRESH_TOKEN_DETAIL;
    }
}