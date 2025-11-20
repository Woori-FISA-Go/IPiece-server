package com.masterpiece.IPiece.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

//필요한 에러코드와 메세지 추가하며 사용 예정

@Getter
public enum ErrorCode {

    /* 공통 - 4xx */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),   //미로그인
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),   //위조,형식 오류
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),   //토큰만료
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 충돌했습니다."),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "도메인 규칙 위반입니다."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다."),

    /* 인증/로그인 */
    INVALID_LOGIN_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증번호 발송 실패"),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다."),
    UNVERIFIED_USER(HttpStatus.BAD_REQUEST, "본인인증이 완료되지 않았습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    LOGOUT_FAILED(HttpStatus.BAD_REQUEST, "로그아웃 실패"),
    TOO_MANY_VERIFICATION_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "인증번호 입력 횟수를 초과했습니다."),

    /* 상품 */
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_OFFERING(HttpStatus.BAD_REQUEST, "공모중인 상품이 아닙니다."),
    OFFERING_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "공모 정보를 찾을 수 없습니다."),
    PRODUCT_ALREADY_IN_TRADE_STATUS(HttpStatus.BAD_REQUEST, "이미 2차거래(TRADE) 상태인 상품입니다."),
    PRODUCT_STATUS_NOT_OFFERING(HttpStatus.CONFLICT, "공모(OFFERING) 상태가 아니어서 2차거래를 시작할 수 없습니다."),
    INVALID_SECONDARY_TRADING_CONFIRM(HttpStatus.BAD_REQUEST, "2차거래 시작 승인 요청의 confirm 값이 true가 아닙니다."),


    /* 공통 - 5xx */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스 이용이 불가합니다."),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "요청 처리 시간이 초과되었습니다."),

    /* 거래/주문  4xx */
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액이 유효하지 않습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.UNPROCESSABLE_ENTITY, "잔액이 부족합니다."),
    MARKET_CLOSED(HttpStatus.UNPROCESSABLE_ENTITY, "현재 거래가 불가능한 시간입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다."),

    /* 공모 */
    INSUFFICIENT_OFFERING_STOCK(HttpStatus.UNPROCESSABLE_ENTITY, "공모 가능 수량이 부족합니다."),
    VIRTUAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 가상계좌를 찾을 수 없습니다."),
    OFFERING_AMOUNT_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "공모 총 수량이 올바르지 않습니다."),
    DUPLICATE_ORDER(HttpStatus.CONFLICT, "중복된 주문입니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "공모 가능한 수량이 없습니다."),

    /* 상품/블록체인 */
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰 정보를 찾을 수 없습니다."),
    CONTRACT_ADDRESS_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "컨트랙트 주소가 등록되지 않았습니다."),
    BLOCKCHAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "블록체인 연동 중 오류가 발생했습니다.");



    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
