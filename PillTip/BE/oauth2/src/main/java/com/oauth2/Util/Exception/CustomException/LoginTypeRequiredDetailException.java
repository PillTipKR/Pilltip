package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class LoginTypeRequiredDetailException extends IllegalStateException {
    private final ErrorCode errorCode;

    public LoginTypeRequiredDetailException() {
        super(ErrorCode.LOGIN_TYPE_REQUIRED_DETAIL.getMessage());
        this.errorCode = ErrorCode.LOGIN_TYPE_REQUIRED_DETAIL;
    }
}