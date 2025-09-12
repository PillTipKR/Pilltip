package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class JsonConversionFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public JsonConversionFailedException() {
        super(ErrorCode.JSON_CONVERSION_FAILED.getMessage());
        this.errorCode = ErrorCode.JSON_CONVERSION_FAILED;
    }
}