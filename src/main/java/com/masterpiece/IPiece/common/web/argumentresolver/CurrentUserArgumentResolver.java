package com.masterpiece.IPiece.common.web.argumentresolver;

import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentUser} 어노테이션이 붙은 메소드 파라미터에 현재 인증된 사용자의 ID (Long)를 주입하는 Argument Resolver.
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @CurrentUser 어노테이션이 붙어있고, 타입이 Long인 경우에만 이 Resolver가 동작합니다.
        return parameter.hasParameterAnnotation(CurrentUser.class) && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 인증되지 않은 사용자이거나 익명 사용자일 경우 null 반환 (혹은 예외 처리)
            // @PreAuthorize 등으로 이미 걸러지겠지만, 방어적으로 처리
            return null; // 또는 throw new AccessDeniedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                // CustomUserDetailsService에서 userId를 username으로 사용했으므로 Long으로 파싱
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                // username이 Long으로 파싱될 수 없는 경우 (예: "admin" 같은 문자열 ID)
                // 이 경우, 해당 사용자는 @CurrentUser Long userId로 받을 수 없음을 의미
                throw new IllegalStateException("Principal username is not a valid Long userId: " + username, e);
            }
        } else if (principal instanceof Long) {
            // 만약 Principal 자체가 Long 타입으로 설정되어 있다면 바로 반환
            return principal;
        }
        // UserDetails 타입이 아니거나 Long 타입이 아닌 경우
        throw new IllegalStateException("Principal is not of type UserDetails or Long: " + principal.getClass().getName());
    }
}
