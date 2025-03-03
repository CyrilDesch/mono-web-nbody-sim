package cyrildeschamps.core.service.simulation.physics;

import cyrildeschamps.core.service.simulation.Body;
import java.util.List;

public class PhysicsEngine {
    private static final float G = 0.1F; // Simplified gravitational constant
    private static final float TIME_STEP = 0.05F; // Time step for integration

    public void updatePositions(List<Body> bodies) {
        bodies.stream()
              .filter(b -> !b.isBlackHole())
              .forEach(b -> b.updatePosition(TIME_STEP));
    }

    public void calculateGravitationalForces(List<Body> bodies) {
        bodies.forEach(Body::resetForce);
        
        // Pour chaque corps non trou noir
        bodies.stream()
              .filter(b -> !b.isBlackHole())
              .forEach(body -> {
                  // Appliquer la force de tous les trous noirs
                  bodies.stream()
                        .filter(Body::isBlackHole)
                        .forEach(blackHole -> body.addForce(blackHole));
              });
    }

    public void updateVelocities(List<Body> bodies) {
        bodies.stream()
              .filter(b -> !b.isBlackHole())
              .forEach(b -> b.updateVelocity(TIME_STEP));
    }

    public float getTimeStep() {
        return TIME_STEP;
    }
} 