package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;


@Getter
public class ValidationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ValidationFailedException() {
        super(ErrorCode.VALIDATION_FAILED.getMessage());
        this.errorCode = ErrorCode.VALIDATION_FAILED;
    }
}