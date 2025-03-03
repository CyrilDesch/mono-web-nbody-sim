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

    @Test
    void particlesShouldHaveInitialVelocity() {
        NBodyService service = new NBodyService();
        service.init();
        
        service.getBodies().stream()
            .skip(1) // Skip black hole
            .forEach(body -> {
                Vector3D velocity = body.getVelocity();
                assert !velocity.equals(new Vector3D(0, 0, 0)) : 
                    "Particles should have non-zero initial velocity";
            });
    }

    @Test
    void simulationShouldUpdatePositions() {
        NBodyService service = new NBodyService();
        service.init();
        
        // Store initial positions
        List<Vector3D> initialPositions = service.getBodies().stream()
            .map(Body::getPosition)
            .collect(Collectors.toList());
        
        // Wait for a short time to allow the simulation to update
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Get updated positions
        List<Vector3D> updatedPositions = service.getBodies().stream()
            .map(Body::getPosition)
            .collect(Collectors.toList());
        
        // Check that at least some positions have changed
        boolean positionsChanged = false;
        for (int i = 0; i < initialPositions.size(); i++) {
            if (!initialPositions.get(i).equals(updatedPositions.get(i))) {
                positionsChanged = true;
                break;
            }
        }
        
        assert positionsChanged : "At least some positions should change during simulation";
    }

    @Test
    void particlesShouldStayInReasonableRange() {
        NBodyService service = new NBodyService();
        service.init();
        
        // Let the simulation run for a short time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check that particles haven't moved too far
        service.getBodies().stream()
            .skip(1) // Skip black hole
            .forEach(body -> {
                float distance = (float) Math.sqrt(
                    body.getX() * body.getX() + 
                    body.getY() * body.getY() + 
                    body.getZ() * body.getZ()
                );
                // Allow for a reasonable range of movement (adjust as needed)
                assert distance < 1000.0f : 
                    "Particles should stay within a reasonable distance from origin";
            });
    }
} 