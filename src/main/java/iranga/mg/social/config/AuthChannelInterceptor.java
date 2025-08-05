package iranga.mg.social.config;

import java.nio.file.attribute.UserPrincipal;

import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import iranga.mg.social.auth.ChatUserDetailsService;
import iranga.mg.social.security.JwtUtil;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthChannelInterceptor.class);
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            logger.info("Authorization Header: {}", authorizationHeader);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7);
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                     userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Authentication error", e);
                    // Optionally throw an exception to deny connection
                }
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                logger.warn("Unauthenticated user tried to send or subscribe. Denying access.");
                throw new AccessDeniedException("Access is denied. You must be authenticated.");
            }
        }
        return message;
    }
}
