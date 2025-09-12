package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidPhoneNumberFormatException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidPhoneNumberFormatException() {
        super(ErrorCode.INVALID_PHONE_NUMBER_FORMAT.getMessage());
        this.errorCode = ErrorCode.INVALID_PHONE_NUMBER_FORMAT;
    }
}