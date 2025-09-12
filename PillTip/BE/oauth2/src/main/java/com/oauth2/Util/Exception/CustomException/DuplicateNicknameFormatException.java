package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateNicknameFormatException extends IllegalStateException {
    private final ErrorCode errorCode;

    public DuplicateNicknameFormatException() {
        super("이미 사용 중인 닉네임입니다.");
        this.errorCode = ErrorCode.DUPLICATE_NICKNAME_FORMAT;
    }
}