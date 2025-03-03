package cyrildeschamps.core.service.simulation;

import cyrildeschamps.core.service.simulation.physics.Vector3D;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BodyTest {

    @Test
    void testBodyInitialization() {
        Body body = new Body();
        assert body.getMass() == 1.0f : "Default mass should be 1.0";
        assert !body.isBlackHole() : "Default body should not be a black hole";
        assert body.getPosition().equals(new Vector3D(0, 0, 0)) : "Default position should be (0,0,0)";
        assert body.getVelocity().equals(new Vector3D(0, 0, 0)) : "Default velocity should be (0,0,0)";
    }

    @Test
    void testSetPosition() {
        Body body = new Body();
        Vector3D newPosition = new Vector3D(1.0f, 2.0f, 3.0f);
        body.setPosition(newPosition);
        assert body.getPosition().equals(newPosition) : "Position should be updated correctly";
        assert body.getX() == 1.0f : "X coordinate should be 1.0";
        assert body.getY() == 2.0f : "Y coordinate should be 2.0";
        assert body.getZ() == 3.0f : "Z coordinate should be 3.0";
    }

    @Test
    void testSetVelocity() {
        Body body = new Body();
        Vector3D newVelocity = new Vector3D(4.0f, 5.0f, 6.0f);
        body.setVelocity(newVelocity);
        assert body.getVelocity().equals(newVelocity) : "Velocity should be updated correctly";
    }

    @Test
    void testSetMass() {
        Body body = new Body();
        float newMass = 10.0f;
        body.setMass(newMass);
        assert body.getMass() == newMass : "Mass should be updated correctly";
    }

    @Test
    void testSetBlackHole() {
        Body body = new Body();
        body.setBlackHole(true);
        assert body.isBlackHole() : "Black hole flag should be updated correctly";
    }

    @Test
    void testUpdatePosition() {
        Body body = new Body();
        Vector3D initialPosition = new Vector3D(1.0f, 1.0f, 1.0f);
        Vector3D velocity = new Vector3D(2.0f, 2.0f, 2.0f);
        body.setPosition(initialPosition);
        body.setVelocity(velocity);
        
        float deltaTime = 0.5f;
        body.updatePosition(deltaTime);
        
        Vector3D expectedPosition = new Vector3D(2.0f, 2.0f, 2.0f);
        assert body.getPosition().equals(expectedPosition) : 
            "Position should be updated correctly based on velocity and time";
    }
} 