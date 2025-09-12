package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class Oauth2UserIdRequiredException extends IllegalStateException {
    private final ErrorCode errorCode;

    public Oauth2UserIdRequiredException() {
        super(ErrorCode.OAUTH2_USER_ID_REQUIRED.getMessage());
        this.errorCode = ErrorCode.OAUTH2_USER_ID_REQUIRED;
    }
}