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
    protected static final int UPDATE_DELAY_MS = 15; // ~60 FPS
    protected static final int NB_PARTICLES = 500;
    protected static final float BLACK_HOLE_MASS = 5e5F;
    protected static final float R_MIN = 50;
    protected static final float R_MAX = 300;

    @Getter
    private final List<Body> bodies = Collections.synchronizedList(new ArrayList<>());
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    volatile boolean running = true;

    @PostConstruct
    void init() {
        initBodies();
        startSimulationLoop();
    }

    void initBodies() {
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

    void updateSimulation() {
        synchronized (bodies) {
            physicsEngine.updatePositions(bodies);
            physicsEngine.calculateGravitationalForces(bodies);
            physicsEngine.updateVelocities(bodies);
            
            // Keep black hole at center if it exists
            bodies.stream()
                .filter(Body::isBlackHole)
                .findFirst()
                .ifPresent(blackHole -> {
                    blackHole.setPosition(new Vector3D());
                    blackHole.setVelocity(new Vector3D());
                });
        }
    }

    public void stopSimulation() {
        running = false;
    }

    /**
     * Crée et ajoute un nouveau corps à la simulation
     * @param x Position x initiale
     * @param y Position y initiale
     * @param z Position z initiale
     * @param mass Masse du corps
     * @param blackHole Si c'est un trou noir
     * @param vx Vitesse initiale en x
     * @param vy Vitesse initiale en y
     * @param vz Vitesse initiale en z
     * @return Le corps créé
     */
    public Body createBody(float x, float y, float z, float mass, boolean blackHole, float vx, float vy, float vz) {
        synchronized (bodies) {
            Body body = new Body();
            body.setPosition(new Vector3D(x, y, z));
            body.setVelocity(new Vector3D(vx, vy, vz));
            body.setMass(mass);
            body.setBlackHole(blackHole);
            bodies.add(body);
            return body;
        }
    }

    /**
     * Supprime un corps de la simulation à l'index spécifié
     * @param index L'index du corps à supprimer
     * @return true si le corps a été supprimé, false sinon
     */
    public boolean deleteBody(int index) {
        synchronized (bodies) {
            if (index < 0 || index >= bodies.size()) {
                return false;
            }
            bodies.remove(index);
            return true;
        }
    }

    public void resetSimulation() {
        synchronized (bodies) {
            bodies.clear();
            initBodies();
        }
    }
}
