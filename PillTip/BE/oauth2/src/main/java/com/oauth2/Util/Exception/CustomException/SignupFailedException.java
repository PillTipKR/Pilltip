package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class SignupFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public SignupFailedException() {
        super(ErrorCode.SIGNUP_FAILED.getMessage());
        this.errorCode = ErrorCode.SIGNUP_FAILED;
    }
}