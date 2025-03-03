package cyrildeschamps.gateway.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

@Getter
@JsonTypeName("deleteBody")
public class DeleteBodyMessage extends WebSocketMessage {
    private final int index;

    @JsonCreator
    public DeleteBodyMessage(@JsonProperty("index") int index) {
        this.index = index;
    }
} 