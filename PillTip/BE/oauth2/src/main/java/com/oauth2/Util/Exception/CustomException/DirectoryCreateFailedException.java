package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DirectoryCreateFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DirectoryCreateFailedException() {
        super(ErrorCode.DIRECTORY_CREATE_FAILED.getMessage());
        this.errorCode = ErrorCode.DIRECTORY_CREATE_FAILED;
    }
}