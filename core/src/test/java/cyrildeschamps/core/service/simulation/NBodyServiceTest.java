package cyrildeschamps.core.service.simulation;

import cyrildeschamps.core.service.simulation.physics.Vector3D;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

@QuarkusTest
class NBodyServiceTest {

    @Test
    void initShouldCreateCorrectNumberOfBodies() {
        NBodyService service = new NBodyService();
        service.init();

        assert service.getBodies().size() == 1501; // 1 black hole + 1500 particles
    }

    @Test
    void initShouldCreateBlackHoleWithCorrectProperties() {
        NBodyService service = new NBodyService();
        service.init();
        
        Body blackHole = service.getBodies().getFirst();
        assert blackHole.isBlackHole();
        assert blackHole.getMass() == 5e5;
        assert blackHole.getPosition().equals(new Vector3D(0, 0, 0));
        assert blackHole.getVelocity().equals(new Vector3D(0, 0, 0));
    }

    @Test
    void initShouldCreateParticlesInValidRange() {
        NBodyService service = new NBodyService();
        service.init();
        
        service.getBodies().stream()
            .skip(1) // Skip black hole
            .forEach(body -> {
                float r = (float) Math.sqrt(body.getX() * body.getX() + body.getY() * body.getY());
                assert r >= 50.0f && r <= 300.0f : "Radius should be between 50 and 300";
                assert body.getZ() == 0 : "Z coordinate should be 0";
                assert body.getMass() == 1.0 : "Mass should be 1.0";
                assert !body.isBlackHole() : "Should not be a black hole";
            });
    }

    @Test
    void stopSimulationShouldStopTheSimulation() {
        NBodyService service = new NBodyService();
        service.init();
        
        service.stopSimulation();
        
        // Wait a bit to ensure the simulation has stopped
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Store initial positions
        List<Vector3D> initialPositions = service.getBodies().stream()
            .map(Body::getPosition)
            .collect(Collectors.toList());
        
        // Wait again to see if positions change
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Compare positions
        List<Vector3D> finalPositions = service.getBodies().stream()
            .map(Body::getPosition)
            .collect(Collectors.toList());
        
        assert initialPositions.equals(finalPositions) : "Positions should not change after stopping simulation";
    }
} 