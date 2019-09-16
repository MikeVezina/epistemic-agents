package eis.messages;

import com.google.gson.reflect.TypeToken;
import eis.agent.AgentAuthentication;
import eis.agent.AgentContainer;
import eis.agent.AgentLocation;
import eis.percepts.MapPercept;
import utils.Position;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Message {
    public static final String CONTENT_TYPE_RESET = "reset";
    public static final String CONTENT_TYPE_PERCEPT = "percept";
    public static final String CONTENT_TYPE_AUTH_AGENTS = "authenticatedAgents";
    public static final String CONTENT_TYPE_LOCATION = "location";

    // This is needed to serialize List<MapPercept> using Gson
    public static final Type MAP_PERCEPT_LIST_TYPE = new TypeToken<Collection<MapPercept>>(){}.getType();
    public static final Type MAP_AUTH_MAP_TYPE = new TypeToken<List<Position>>(){}.getType();
    public static final String CONTENT_TYPE_NEW_STEP = "newStep";
    private static final ExecutorService parseExecutor = Executors.newSingleThreadExecutor();

    private String contentType;
    private String messageBody;

    private Message(String contentType, String messageBody) {
        this.contentType = contentType;
        this.messageBody = messageBody;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public static void createAndSendNewStepMessage(MQSender mqSender, long step) {
        if(mqSender == null)
            return;

        parseExecutor.submit(() -> {
            Message msg = new Message(CONTENT_TYPE_NEW_STEP, String.valueOf(step));
            mqSender.sendMessage(msg);
        });
    }

    public static void createAndSendResetMessage(MQSender mqSender) {
        if(mqSender == null)
            return;

        parseExecutor.submit(() -> {
            Message msg = new Message(CONTENT_TYPE_RESET, "");
            mqSender.sendMessage(msg);
        });
    }

    public static void createAndSendAuthenticatedMessage(MQSender mqSender, List<AgentContainer> authenticatedContainers, Map<String, Position> translationValues) {

        if(mqSender == null)
            return;

        parseExecutor.submit(() -> {
            Map<Position, String> agentPositions = new HashMap<>();
            for(AgentContainer agentContainer : authenticatedContainers)
            {
                Position translation = translationValues.get(agentContainer.getAgentName());
                Position translatedPosition = agentContainer.getCurrentLocation().add(translation);
                agentPositions.put(translatedPosition, agentContainer.getAgentName());
            }

            Message msg = new Message(CONTENT_TYPE_AUTH_AGENTS, GsonInstance.getInstance().toJson(agentPositions.keySet(), MAP_AUTH_MAP_TYPE));
            mqSender.sendMessage(msg);
        });
    }

    public static void createAndSendPerceptMessage(MQSender mqSender, AgentLocation location, Collection<MapPercept> mapPercept) {

        if (mapPercept == null || mqSender == null || location == null)
            return;

        parseExecutor.submit(() -> {
            Message locMsg = new Message(CONTENT_TYPE_LOCATION, location.toJsonString());
            mqSender.sendMessage(locMsg);

            Message msg = new Message(CONTENT_TYPE_PERCEPT, GsonInstance.getInstance().toJson(mapPercept, MAP_PERCEPT_LIST_TYPE));
            mqSender.sendMessage(msg);
        });
    }
}
