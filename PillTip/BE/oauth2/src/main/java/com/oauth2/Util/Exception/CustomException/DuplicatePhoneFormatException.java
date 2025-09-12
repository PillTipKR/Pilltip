package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicatePhoneFormatException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DuplicatePhoneFormatException() {
        super("전화번호가 중복인 것 같습니다. 다른 전화번호를 입력해주세요.");
        this.errorCode = ErrorCode.DUPLICATE_PHONE_FORMAT;
    }
}