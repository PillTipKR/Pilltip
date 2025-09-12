package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class JsonParseFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public JsonParseFailedException() {
        super(ErrorCode.JSON_PARSE_FAILED.getMessage());
        this.errorCode = ErrorCode.JSON_PARSE_FAILED;
    }
}