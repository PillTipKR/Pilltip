package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class UserIdPasswordRequiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public UserIdPasswordRequiredException() {
        super(ErrorCode.USER_ID_PASSWORD_REQUIRED.getMessage());
        this.errorCode = ErrorCode.USER_ID_PASSWORD_REQUIRED;
    }
}