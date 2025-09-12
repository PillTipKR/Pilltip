package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DeserializationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DeserializationFailedException() {
        super(ErrorCode.DESERIALIZATION_FAILED.getMessage());
        this.errorCode = ErrorCode.DESERIALIZATION_FAILED;
    }
}