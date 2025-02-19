package cyrildeschamps.core.service.simulation.physics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector3D {
    private float x, y, z;

    public Vector3D() {
        this(0, 0, 0);
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    public Vector3D multiply(float scalar) {
        return new Vector3D(x * scalar, y * scalar, z * scalar);
    }

    public float distance(Vector3D other) {
        float dx = other.x - this.x;
        float dy = other.y - this.y;
        float dz = other.z - this.z;
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
} 