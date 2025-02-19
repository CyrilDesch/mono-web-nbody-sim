package cyrildeschamps.gateway.DTO;

import cyrildeschamps.core.service.simulation.Body;
import lombok.Data;

@Data
public class BodyDTO {
    private float x, y, z;
    private boolean blackHole;

    public BodyDTO(Body body) {
        this.x = body.getX();
        this.y = body.getY();
        this.z = body.getZ();
        this.blackHole = body.isBlackHole();
    }
}
