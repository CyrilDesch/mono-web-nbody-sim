package cyrildeschamps.gateway.websocket.manager;

import cyrildeschamps.core.service.simulation.Body;
import cyrildeschamps.core.service.simulation.NBodyService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class NBodySessionManagerTest {

    @Inject
    NBodySessionManager sessionManager;

    @InjectMock
    NBodyService nBodyService;

    private Session session;
    private RemoteEndpoint.Basic basicRemote;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);
        when(session.isOpen()).thenReturn(true);
        
        List<Body> mockBodies = new ArrayList<>();
        when(nBodyService.getBodies()).thenReturn(mockBodies);
    }

    @Test
    void subscribeShouldStartSendingUpdates() throws IOException, InterruptedException {
        // Given
        int fps = 30;

        // When
        sessionManager.subscribe(session, fps);

        // Then - Wait a bit to ensure at least one message is sent
        Thread.sleep(100);
        verify(basicRemote, atLeastOnce()).sendText(anyString());
    }

    @Test
    void cancelSubscriptionShouldStopSendingUpdates() throws IOException, InterruptedException {
        // Given
        int fps = 30;
        sessionManager.subscribe(session, fps);

        // When
        sessionManager.cancelSubscription(session);

        // Then
        Mockito.reset(basicRemote);
        Thread.sleep(100);
        verify(basicRemote, never()).sendText(anyString());
    }

    @Test
    void shouldStopSendingUpdatesWhenSessionIsClosed() throws IOException, InterruptedException {
        // Given
        int fps = 30;
        when(session.isOpen()).thenReturn(false);

        // When
        sessionManager.subscribe(session, fps);

        // Then
        Thread.sleep(100);
        verify(basicRemote, never()).sendText(anyString());
    }

    @Test
    void subscribingTwiceShouldCancelPreviousSubscription() throws IOException, InterruptedException {
        // Given
        sessionManager.subscribe(session, 30);
        Mockito.reset(basicRemote);

        // When
        sessionManager.subscribe(session, 60);
        Thread.sleep(100);

        // Then - Should only have messages from the second subscription
        verify(basicRemote, atLeastOnce()).sendText(anyString());
    }

    @Test
    void deleteBodyShouldDelegateToService() {
        // Given
        when(nBodyService.deleteBody(anyInt())).thenReturn(true);

        // When
        boolean result = sessionManager.deleteBody(1);

        // Then
        verify(nBodyService).deleteBody(1);
        assert result : "Should return the result from the service";
    }

    @Test
    void deleteBodyShouldHandleFailure() {
        // Given
        when(nBodyService.deleteBody(anyInt())).thenReturn(false);

        // When
        boolean result = sessionManager.deleteBody(1);

        // Then
        verify(nBodyService).deleteBody(1);
        assert !result : "Should return false when service fails to delete";
    }
} 