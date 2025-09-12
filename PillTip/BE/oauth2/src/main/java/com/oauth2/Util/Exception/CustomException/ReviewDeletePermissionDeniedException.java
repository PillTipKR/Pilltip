package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class ReviewDeletePermissionDeniedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public ReviewDeletePermissionDeniedException() {
        super(ErrorCode.REVIEW_DELETE_PERMISSION_DENIED.getMessage());
        this.errorCode = ErrorCode.REVIEW_DELETE_PERMISSION_DENIED;
    }
}