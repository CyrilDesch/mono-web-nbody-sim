package cyrildeschamps.core.service.simulation;

import cyrildeschamps.core.service.simulation.physics.Vector3D;
import lombok.Data;

@Data
public class Body {
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D force;
    private float mass;
    private boolean blackHole;

    public Body() {
        this.position = new Vector3D();
        this.velocity = new Vector3D();
        this.force = new Vector3D();
        this.mass = 1.0F;
        this.blackHole = false;
    }

    public void resetForce() {
        this.force = new Vector3D();
    }

    /**
     * Force gravitationnelle exercée par 'other' sur 'this'.
     * If you want to keep the Z axis, you can keep it, otherwise ignore it
     */
    public void addForce(Body other) {
        float G = 0.1F;
        Vector3D direction = other.position.subtract(this.position);
        float distance = (float) (this.position.distance(other.position) + 1e-3); // softening
        float forceMagnitude = (G * this.mass * other.mass) / (distance * distance);
        
        Vector3D forceVector = direction.multiply(forceMagnitude / distance);
        this.force = this.force.add(forceVector);
    }

    /**
     * Mise à jour de la position (première étape de Velocity Verlet simplifié).
     * If you have a storage of acceleration, adapt it here (Verlet).
     */
    public void updatePosition(float dt) {
        this.position = this.position.add(velocity.multiply(dt));
    }

    /**
     * Mise à jour de la vitesse (seconde étape).
     * If you have a storage of acceleration, adapt it here (Verlet).
     */
    public void updateVelocity(float dt) {
        Vector3D acceleration = force.multiply((float) (1.0 / mass));
        this.velocity = this.velocity.add(acceleration.multiply(dt));
    }

    // Getters for frontend compatibility
    public float getX() { return position.getX(); }
    public float getY() { return position.getY(); }
    public float getZ() { return position.getZ(); }
    
    // Setters for frontend compatibility
    public void setX(float x) { position = new Vector3D(x, position.getY(), position.getZ()); }
    public void setY(float y) { position = new Vector3D(position.getX(), y, position.getZ()); }
    public void setZ(float z) { position = new Vector3D(position.getX(), position.getY(), z); }
    
    public void setVx(float vx) { velocity = new Vector3D(vx, velocity.getY(), velocity.getZ()); }
    public void setVy(float vy) { velocity = new Vector3D(velocity.getX(), vy, velocity.getZ()); }
    public void setVz(float vz) { velocity = new Vector3D(velocity.getX(), velocity.getY(), vz); }
}
