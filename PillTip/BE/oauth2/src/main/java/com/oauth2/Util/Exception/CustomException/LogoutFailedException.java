package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class LogoutFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public LogoutFailedException() {
        super(ErrorCode.LOGOUT_FAILED.getMessage());
        this.errorCode = ErrorCode.LOGOUT_FAILED;
    }
}