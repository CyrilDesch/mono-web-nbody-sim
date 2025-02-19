package cyrildeschamps.gateway.websocket.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import cyrildeschamps.gateway.websocket.manager.NBodySessionManager;
import cyrildeschamps.gateway.websocket.messages.FpsMessage;
import cyrildeschamps.gateway.websocket.messages.WebSocketMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ServerEndpoint("/nbody")
@ApplicationScoped
public class NBodyWebSocketEndpoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    NBodySessionManager sessionManager;

    @OnOpen
    public void onOpen(Session session) { }

    @OnClose
    public void onClose(Session session) {
        sessionManager.cancelSubscription(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Erreur sur la session {} : {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message, WebSocketMessage.class);
            if (wsMessage instanceof FpsMessage fpsMessage) {
                sessionManager.subscribe(session, fpsMessage.getFps());
            } else {
                log.warn("Type de message non supporté : {}", wsMessage.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message sur la session {} : {}", session.getId(), e.getMessage());
        }
    }
}
