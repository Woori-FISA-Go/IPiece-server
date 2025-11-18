package com.masterpiece.IPiece.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BlockchainException extends BusinessException {
    public BlockchainException(String message) {
        super(ErrorCode.BLOCKCHAIN_ERROR, message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(ErrorCode.BLOCKCHAIN_ERROR, message, cause);
    }
}
