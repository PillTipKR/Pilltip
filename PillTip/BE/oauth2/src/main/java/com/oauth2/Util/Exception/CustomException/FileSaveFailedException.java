package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class FileSaveFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public FileSaveFailedException() {
        super(ErrorCode.FILE_SAVE_FAILED.getMessage());
        this.errorCode = ErrorCode.FILE_SAVE_FAILED;
    }
}