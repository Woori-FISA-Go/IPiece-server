package com.masterpiece.IPiece.common.exception;

import com.masterpiece.IPiece.common.web.Responses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Controller 전역 예외 처리
public class GlobalExceptionHandler {

    /**
     * BusinessException → ErrorCode 기반 처리
     * 프론트에 뿌려줄 JSON형식을 커스터마이징 하는 부분.
     * 기존 예외는 JSON방식을 아래와 같이 뿌려주게 됨
     * {
     *   "timestamp": "2025-11-12T12:01:51.000",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "상품 없음",
     *   "path": "/v1/products/10"
     * }
     * 이렇게 되면 message를 보고 프론트에서 에러를 구분하게 됨 -> 유지보수성x
     * 당장 우리 프로젝트에 실행하는데에는 문제가 없지만 좋은 코드가 아님
     * 따라서 Handler에서 모든 오류 잡아서 우리가 정의한 JSON형식으로 뿌려줌
     *  -> 프론트에서는 code.name()으로만 구분하면 됨(ENUM타입이기 때문에 바뀔일x)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, HttpServletRequest request) {

        ErrorCode code = e.getErrorCode();

        return Responses.problem(
                code.getStatus(),
                code.name(),
                code.getMessage(),
                code.getMessage(),
                request.getRequestURI(), // ← null 아님
                null
        );
    }

    /**
     * 예상하지 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        return Responses.internalError(
                "INTERNAL_SERVER_ERROR",
                "Internal server error",
                e.getMessage(),
                null
        );
    }
}
