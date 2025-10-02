package iranga.mg.social.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import iranga.mg.social.service.UserService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("🔌 Tentative de connexion WebSocket - Session: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Anonyme";
        logger.info("Utilisateur {} s'est connecté", user);
        userService.setUserOnlineStatus(user, true);
        logger.info("✅ WebSocket connecté - Session: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Anonyme";
        logger.info("Utilisateur {} s'est déconnecté", user);
        userService.setUserOnlineStatus(user, false);
        logger.info("❌ Déconnexion WebSocket - Session: {}", sessionId);
    }
}

