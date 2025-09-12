package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class SearchFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public SearchFailedException() {
        super(ErrorCode.SEARCH_FAILED.getMessage());
        this.errorCode = ErrorCode.SEARCH_FAILED;
    }
}