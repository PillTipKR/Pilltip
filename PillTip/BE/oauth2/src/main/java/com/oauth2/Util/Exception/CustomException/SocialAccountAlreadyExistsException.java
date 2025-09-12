package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class SocialAccountAlreadyExistsException extends IllegalStateException {
    private final ErrorCode errorCode;

    public SocialAccountAlreadyExistsException() {
        super(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getMessage());
        this.errorCode = ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS;
    }
}