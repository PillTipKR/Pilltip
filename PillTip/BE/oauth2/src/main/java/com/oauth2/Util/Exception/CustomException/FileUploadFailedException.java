package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class FileUploadFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public FileUploadFailedException() {
        super(ErrorCode.FILE_UPLOAD_FAILED.getMessage());
        this.errorCode = ErrorCode.FILE_UPLOAD_FAILED;
    }
}