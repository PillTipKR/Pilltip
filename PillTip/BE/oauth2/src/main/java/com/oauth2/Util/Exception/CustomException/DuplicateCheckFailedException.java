package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateCheckFailedException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DuplicateCheckFailedException() {
        super("이미 사용 중인 %s입니다.");
        this.errorCode = ErrorCode.DUPLICATE_CHECK_FAILED;
    }
}