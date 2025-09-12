package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class AuthenticationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public AuthenticationFailedException() {
        super(ErrorCode.AUTHENTICATION_FAILED.getMessage());
        this.errorCode = ErrorCode.AUTHENTICATION_FAILED;
    }
}