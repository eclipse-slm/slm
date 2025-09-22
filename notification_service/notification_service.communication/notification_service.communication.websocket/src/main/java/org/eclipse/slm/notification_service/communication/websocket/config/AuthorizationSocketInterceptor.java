package org.eclipse.slm.notification_service.communication.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.eclipse.slm.common.utils.keycloak.CustomJwtDecoder;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class AuthorizationSocketInterceptor implements ChannelInterceptor {

    private final static Logger LOG = LoggerFactory.getLogger(AuthorizationSocketInterceptor.class);

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (nonNull(accessor)) {
            switch (accessor.getCommand()) {
                //Authenticate user on CONNECT
                case CONNECT -> {
                    //Extract JWT token from header, validate it and extract user authorities
                    var authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (isNull(authHeader) || !authHeader.startsWith("Bearer" + " ")) {
                        // If there is no token present then we should interrupt handshake process and throw an AccessDeniedException
                        throw new AccessDeniedException("WebSocketSecurityConfig.WS_UNAUTHORIZED_MESSAGE");
                    }
                    var token = authHeader.substring("Bearer".length() + 1);

                    var issuerHeader = accessor.getFirstNativeHeader("Issuer");
                    if (isNull(issuerHeader)) {
                        // If there is no issuer present then we should interrupt handshake process and throw an AccessDeniedException
                        throw new AccessDeniedException("WebSocketSecurityConfig.WS_UNAUTHORIZED_MESSAGE");
                    }

                    Jwt jwt;
                    try {
                        //Validate JWT token with any resource server
                        JwtDecoder jwtDecoder = CustomJwtDecoder.fromIssuer(issuerHeader);
                        jwt = jwtDecoder.decode(token);
                    } catch (JwtException ex) {
                        //In case the JWT token is expired or cannot be decoded, an AccessDeniedException should be thrown
                        throw new AccessDeniedException("WebSocketSecurityConfig.WS_UNAUTHORIZED_MESSAGE");
                    }
                    JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, null);
                    accessor.setUser(jwtAuthenticationToken);

                    var userId = jwtAuthenticationToken.getPrincipal();
                    LOG.info("User " + jwt.getClaims().get("preferred_username") + " [id='" + jwt.getSubject() + "'] connected to websocket interface");
                }
                case DISCONNECT -> {
                    var jwtAuthenticationToken = (JwtAuthenticationToken) accessor.getUser();
                    var jwt = jwtAuthenticationToken.getToken();
                    LOG.info("User " + jwt.getClaims().get("preferred_username") + " [id='" + jwt.getSubject() + "'] disconnected from websocket interface");
                }
            }
        }
        return message;
    }
}