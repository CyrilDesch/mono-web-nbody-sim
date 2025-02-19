package cyrildeschamps.gateway.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

@Getter
@JsonTypeName("fps")
public class FpsMessage extends WebSocketMessage {

    private final int fps;

    @JsonCreator
    public FpsMessage(@JsonProperty("fps") int fps) {
        this.fps = fps;
    }
}
