package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class OperationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public OperationFailedException() {
        super(ErrorCode.OPERATION_FAILED.getMessage());
        this.errorCode = ErrorCode.OPERATION_FAILED;
    }
}