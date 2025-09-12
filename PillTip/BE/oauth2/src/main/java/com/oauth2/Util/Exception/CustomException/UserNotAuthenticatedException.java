package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UserNotAuthenticatedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UserNotAuthenticatedException() {
        super(ErrorCode.USER_NOT_AUTHENTICATED.getMessage());
        this.errorCode = ErrorCode.USER_NOT_AUTHENTICATED;
    }
}