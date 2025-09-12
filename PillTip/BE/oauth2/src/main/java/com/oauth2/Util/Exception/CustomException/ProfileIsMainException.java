package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ProfileIsMainException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ProfileIsMainException() {
        super(ErrorCode.PROFILE_IS_MAIN.getMessage());
        this.errorCode = ErrorCode.PROFILE_IS_MAIN;
    }
}