package com.masterpiece.IPiece.common.exception;

public class TokenNotFoundException extends BusinessException {
    public TokenNotFoundException(String message) {
        super(ErrorCode.TOKEN_NOT_FOUND, message);
    }

    public TokenNotFoundException() {
        super(ErrorCode.TOKEN_NOT_FOUND);
    }
}
