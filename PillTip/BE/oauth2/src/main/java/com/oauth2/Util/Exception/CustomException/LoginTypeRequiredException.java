package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class LoginTypeRequiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public LoginTypeRequiredException() {
        super(ErrorCode.LOGIN_TYPE_REQUIRED.getMessage());
        this.errorCode = ErrorCode.LOGIN_TYPE_REQUIRED;
    }
}