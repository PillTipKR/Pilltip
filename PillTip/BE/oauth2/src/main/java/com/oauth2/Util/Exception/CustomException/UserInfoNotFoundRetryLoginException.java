package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UserInfoNotFoundRetryLoginException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UserInfoNotFoundRetryLoginException() {
        super(ErrorCode.USER_INFO_NOT_FOUND_RETRY_LOGIN.getMessage());
        this.errorCode = ErrorCode.USER_INFO_NOT_FOUND_RETRY_LOGIN;
    }
}