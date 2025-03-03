package cyrildeschamps.gateway.websocket.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FpsMessage.class, name = "fps"),
        @JsonSubTypes.Type(value = CreateBodiesMessage.class, name = "createBodies"),
        @JsonSubTypes.Type(value = DeleteBodyMessage.class, name = "deleteBody"),
        @JsonSubTypes.Type(value = ResetSimulationMessage.class, name = "reset")
})
public abstract class WebSocketMessage { }
