package com.masterpiece.IPiece.config;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.user.application.TokenBlacklistService;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern HOLDING_TOPIC =
            Pattern.compile("^/topic/holding/(\\d+)/(\\d+)$");
    private static final Pattern PENDING_TOPIC =
            Pattern.compile("^/topic/pending-orders/(\\d+)/(\\d+)$");

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                authenticate(accessor); // late auth for clients that skip CONNECT auth
            }
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return; // allow anonymous for public topics, private topics will be blocked later
        }

        String token = authHeader.substring(7);
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new AccessDeniedException("Blacklisted token");
        }

        ErrorCode error = jwtTokenProvider.validateToken(token);
        if (error != null) {
            throw new AccessDeniedException(error.getMessage());
        }

        String userIdStr = jwtTokenProvider.getSubject(token);
        UsernamePasswordAuthenticationToken authentication = buildAuthentication(userIdStr);

        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String userIdStr) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userIdStr);
            Long userId = Long.valueOf(userIdStr);
            return new UsernamePasswordAuthenticationToken(userId, null, userDetails.getAuthorities());
        } catch (Exception ex) {
            throw new AccessDeniedException("Invalid token subject");
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Long principalUserId = extractUserId(accessor.getUser());

        if (isUserScopedTopic(destination)) {
            if (principalUserId == null) {
                throw new AccessDeniedException("Authentication required for private topic");
            }

            Long pathUserId = extractUserIdFromPath(destination);
            if (pathUserId == null || !pathUserId.equals(principalUserId)) {
                throw new AccessDeniedException("Cannot subscribe to another user's topic");
            }
        }
    }

    private boolean isUserScopedTopic(String destination) {
        return HOLDING_TOPIC.matcher(destination).matches()
                || PENDING_TOPIC.matcher(destination).matches();
    }

    private Long extractUserIdFromPath(String destination) {
        Matcher holdingMatcher = HOLDING_TOPIC.matcher(destination);
        if (holdingMatcher.matches()) {
            return Long.valueOf(holdingMatcher.group(1));
        }

        Matcher pendingMatcher = PENDING_TOPIC.matcher(destination);
        if (pendingMatcher.matches()) {
            return Long.valueOf(pendingMatcher.group(1));
        }

        return null;
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof Long id) {
                return id;
            }
            if (principalObj instanceof String idStr) {
                try {
                    return Long.valueOf(idStr);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
