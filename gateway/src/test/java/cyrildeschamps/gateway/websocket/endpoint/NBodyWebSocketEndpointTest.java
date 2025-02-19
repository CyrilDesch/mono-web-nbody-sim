package cyrildeschamps.gateway.websocket.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import cyrildeschamps.gateway.websocket.manager.NBodySessionManager;
import cyrildeschamps.gateway.websocket.messages.FpsMessage;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@QuarkusTest
class NBodyWebSocketEndpointTest {

    @Inject
    NBodyWebSocketEndpoint endpoint;

    @InjectMock
    NBodySessionManager sessionManager;

    private Session session;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        when(session.getId()).thenReturn("test-session-id");
    }

    @Test
    void onMessageShouldHandleFpsMessage() throws Exception {
        // Given
        FpsMessage fpsMessage = new FpsMessage(30);
        String message = objectMapper.writeValueAsString(fpsMessage);

        // When
        endpoint.onMessage(message, session);

        // Then
        verify(sessionManager).subscribe(eq(session), eq(30));
    }

    @Test
    void onCloseShouldCancelSubscription() {
        // When
        endpoint.onClose(session);

        // Then
        verify(sessionManager).cancelSubscription(session);
    }

    @Test
    void onMessageShouldHandleInvalidJson() {
        // Given
        String invalidJson = "invalid json";

        // When
        endpoint.onMessage(invalidJson, session);

        // Then
        verify(sessionManager, never()).subscribe(any(), anyInt());
    }

    @Test
    void onOpenShouldNotThrowException() {
        // When/Then - should not throw
        endpoint.onOpen(session);
    }

    @Test
    void onErrorShouldNotThrowException() {
        // Given
        Exception testException = new RuntimeException("Test exception");

        // When/Then - should not throw
        endpoint.onError(session, testException);
    }
} 