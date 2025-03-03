package cyrildeschamps.gateway.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

@Getter
@JsonTypeName("createBodies")
public class CreateBodiesMessage extends WebSocketMessage {
    private final int count;
    private final float range;
    private final boolean blackHole;

    @JsonCreator
    public CreateBodiesMessage(
            @JsonProperty("count") int count,
            @JsonProperty("range") float range,
            @JsonProperty("blackHole") boolean blackHole) {
        this.count = count;
        this.range = range;
        this.blackHole = blackHole;
    }
} 