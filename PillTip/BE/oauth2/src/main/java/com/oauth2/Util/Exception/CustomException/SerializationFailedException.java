package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class SerializationFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public SerializationFailedException() {
        super(ErrorCode.SERIALIZATION_FAILED.getMessage());
        this.errorCode = ErrorCode.SERIALIZATION_FAILED;
    }
}