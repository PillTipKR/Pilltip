package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class GoogleUserIdRequiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public GoogleUserIdRequiredException() {
        super(ErrorCode.GOOGLE_USER_ID_REQUIRED.getMessage());
        this.errorCode = ErrorCode.GOOGLE_USER_ID_REQUIRED;
    }
}