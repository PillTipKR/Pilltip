package com.oauth2.Util.Exception.CustomException;


import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class NotFriendException extends IllegalStateException {
    private final ErrorCode errorCode;

    public NotFriendException() {
        super(ErrorCode.NOT_FRIEND.getMessage());
        this.errorCode = ErrorCode.NOT_FRIEND;
    }

}
