package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorKeywordPhoneNumberException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ErrorKeywordPhoneNumberException() {
        super(ErrorCode.ERROR_KEYWORD_PHONE_NUMBER.getMessage());
        this.errorCode = ErrorCode.ERROR_KEYWORD_PHONE_NUMBER;
    }
}