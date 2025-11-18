package com.masterpiece.IPiece.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WalletNotFoundException extends BusinessException {
    public WalletNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
