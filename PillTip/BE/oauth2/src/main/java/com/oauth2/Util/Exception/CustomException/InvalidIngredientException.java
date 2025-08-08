package com.oauth2.Util.Exception.CustomException;

import com.oauth2.Util.Exception.Model.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidIngredientException extends IllegalStateException {
    private final ErrorCode errorCode;

    public InvalidIngredientException() {
        super(ErrorCode.INVALID_INGREDIENT.getMessage());
        this.errorCode = ErrorCode.INVALID_INGREDIENT;
    }
}