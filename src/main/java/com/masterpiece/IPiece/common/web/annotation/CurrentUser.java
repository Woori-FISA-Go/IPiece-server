package com.masterpiece.IPiece.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자의 ID (Long)를 메소드 파라미터에 주입하기 위한 커스텀 어노테이션.
 * UserDetails 객체에서 userId를 추출하는 로직을 추상화합니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal // Spring Security의 @AuthenticationPrincipal을 메타 어노테이션으로 사용
public @interface CurrentUser {
}
