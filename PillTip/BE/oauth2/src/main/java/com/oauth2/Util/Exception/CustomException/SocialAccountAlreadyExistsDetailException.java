package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class SocialAccountAlreadyExistsDetailException extends IllegalStateException {
    private final ErrorCode errorCode;

    public SocialAccountAlreadyExistsDetailException() {
        super(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL.getMessage());
        this.errorCode = ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS_DETAIL;
    }
}