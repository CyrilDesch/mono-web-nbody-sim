package cyrildeschamps.gateway.websocket.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import cyrildeschamps.gateway.DTO.BodyDTO;
import cyrildeschamps.core.service.simulation.Body;
import cyrildeschamps.core.service.simulation.NBodyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@Slf4j
@ApplicationScoped
public class NBodySessionManager {

    private final ConcurrentMap<Session, ScheduledFuture<?>> sessionTasks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Inject
    NBodyService nBodyService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void subscribe(Session session, int fps) {
        cancelSubscription(session);

        Runnable task = () -> {
            if (!session.isOpen()) {
                cancelSubscription(session);
                return;
            }
            try {
                List<Body> bodies = nBodyService.getBodies();
                List<BodyDTO> dtos = bodies.stream().map(BodyDTO::new).toList();
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(Map.of("bodies", dtos)));
            } catch (IOException e) {
                // Ignore, websocket interrupted
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, 1000 / fps, TimeUnit.MILLISECONDS);
        sessionTasks.put(session, future);
    }

    public void cancelSubscription(Session session) {
        ScheduledFuture<?> future = sessionTasks.remove(session);
        if (future != null) {
            future.cancel(true);
        }
    }

    private float randomInRange(float range) {
        return (random.nextFloat() * 2 - 1) * range;
    }

    /**
     * Crée plusieurs corps dans la simulation avec des positions aléatoires
     */
    public void createBodies(int count, float range, boolean blackHole) {
        float defaultVelocity = 2.0f;
        float mass = blackHole ? 5e5f : 1.0f;

        for (int i = 0; i < count; i++) {
            nBodyService.createBody(
                randomInRange(range),  // x
                randomInRange(range),  // y
                randomInRange(range),  // z
                mass,
                blackHole,
                randomInRange(defaultVelocity),  // vx
                randomInRange(defaultVelocity),  // vy
                randomInRange(defaultVelocity)   // vz
            );
        }
    }

    /**
     * Réinitialise la simulation à son état initial
     */
    public void resetSimulation() {
        nBodyService.resetSimulation();
    }

    /**
     * Supprime un corps de la simulation
     */
    public boolean deleteBody(int index) {
        return nBodyService.deleteBody(index);
    }
}
