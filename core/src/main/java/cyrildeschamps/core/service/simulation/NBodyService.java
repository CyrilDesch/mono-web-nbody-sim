package cyrildeschamps.core.service.simulation;

import cyrildeschamps.core.service.simulation.physics.PhysicsEngine;
import cyrildeschamps.core.service.simulation.physics.Vector3D;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class NBodyService {
    private static final int UPDATE_DELAY_MS = 15; // ~60 FPS
    private static final int NB_PARTICLES = 1500;
    private static final float BLACK_HOLE_MASS = 5e5F;
    private static final float R_MIN = 50;
    private static final float R_MAX = 300;

    @Getter
    private final List<Body> bodies = Collections.synchronizedList(new ArrayList<>());
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private volatile boolean running = true;

    @PostConstruct
    void init() {
        initBodies();
        startSimulationLoop();
    }

    private void initBodies() {
        // Initialize black hole
        Body blackHole = new Body();
        blackHole.setMass(BLACK_HOLE_MASS);
        blackHole.setBlackHole(true);
        bodies.add(blackHole);

        // Initialize particles in a ring
        Random rand = new Random();
        for (int i = 0; i < NB_PARTICLES; i++) {
            float r = R_MIN + (R_MAX - R_MIN) * rand.nextFloat();
            float angle = (float) (2 * Math.PI * rand.nextFloat());

            // Calculate position
            float x = (float) (r * Math.cos(angle));
            float y = (float) (r * Math.sin(angle));

            // Calculate initial velocity for circular orbit
            float speed = (float) Math.sqrt((0.1 * BLACK_HOLE_MASS) / r);
            float vx = (float) (-speed * Math.sin(angle));
            float vy = (float) (speed * Math.cos(angle));

            Body star = new Body();
            star.setX(x);
            star.setY(y);
            star.setVx(vx);
            star.setVy(vy);
            bodies.add(star);
        }
    }

    private void startSimulationLoop() {
        new Thread(() -> {
            while (running) {
                updateSimulation();
                try {
                    Thread.sleep(UPDATE_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "NBodySimulationThread").start();
    }

    private void updateSimulation() {
        synchronized (bodies) {
            physicsEngine.updatePositions(bodies);
            physicsEngine.calculateGravitationalForces(bodies);
            physicsEngine.updateVelocities(bodies);
            
            // Keep black hole at center
            Body blackHole = bodies.getFirst();
            blackHole.setPosition(new Vector3D());
            blackHole.setVelocity(new Vector3D());
        }
    }

    public void stopSimulation() {
        running = false;
    }
}
