package cyrildeschamps.gateway.DTO;

import cyrildeschamps.core.service.simulation.Body;
import lombok.Data;

@Data
public class BodyDTO {
    private float x, y, z;
    private float vx, vy, vz;
    private float mass;
    private boolean blackHole;

    public BodyDTO(Body body) {
        this.x = body.getX();
        this.y = body.getY();
        this.z = body.getZ();
        this.vx = body.getVelocity().getX();
        this.vy = body.getVelocity().getY();
        this.vz = body.getVelocity().getZ();
        this.mass = body.getMass();
        this.blackHole = body.isBlackHole();
    }
}
