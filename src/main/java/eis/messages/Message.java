package eis.messages;

import com.google.gson.reflect.TypeToken;
import eis.agent.AgentLocation;
import eis.percepts.MapPercept;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Message {
    public static final String CONTENT_TYPE_RESET = "reset";
    public static final String CONTENT_TYPE_PERCEPT = "percept";
    public static final String CONTENT_TYPE_LOCATION = "location";

    // This is needed to serialize List<MapPercept> using Gson
    public static final Type MAP_PERCEPT_LIST_TYPE = new TypeToken<Collection<MapPercept>>(){}.getType();
    public static final String CONTENT_TYPE_NEW_STEP = "newStep";

    private String contentType;
    private String messageBody;

    private Message(String contentType, String messageBody) {
        this.contentType = contentType;
        this.messageBody = messageBody;
    }

    public static Message createNewStepMessage(long step) {
        return new Message(CONTENT_TYPE_NEW_STEP, String.valueOf(step));
    }

    public String getContentType() {
        return contentType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public static Message createResetMessage() {
        return new Message(CONTENT_TYPE_RESET, "");
    }

    public static Message createPerceptMessage(Collection<MapPercept> mapPercept) {

        if (mapPercept == null)
            return null;

        return new Message(CONTENT_TYPE_PERCEPT, GsonInstance.getInstance().toJson(mapPercept, MAP_PERCEPT_LIST_TYPE));
    }

    public static Message createLocationMessage(AgentLocation location) {
        if (location == null)
            return null;

        return new Message(CONTENT_TYPE_LOCATION, location.toJsonString());
    }
}
