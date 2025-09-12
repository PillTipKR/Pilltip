package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidProfileIdException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidProfileIdException() {
        super(ErrorCode.INVALID_PROFILE_ID.getMessage());
        this.errorCode = ErrorCode.INVALID_PROFILE_ID;
    }
}