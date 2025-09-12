package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class LoginFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED.getMessage());
        this.errorCode = ErrorCode.LOGIN_FAILED;
    }
}