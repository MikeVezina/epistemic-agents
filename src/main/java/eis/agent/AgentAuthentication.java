package eis.agent;

import eis.messages.Message;
import eis.map.MapPercept;
import eis.map.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AgentAuthentication {
    private static final Logger LOG = Logger.getLogger(AgentAuthentication.class.getName());
    private ConcurrentMap<String, Position> translationMap;
    private ConcurrentMap<String, AgentContainer> containerMap;
    private AgentContainer selfAgentContainer;

    public AgentAuthentication(AgentContainer selfAgentContainer) {
        this.selfAgentContainer = selfAgentContainer;
        this.translationMap = new ConcurrentHashMap<>();
        this.containerMap = new ConcurrentHashMap<>();
    }

    public void authenticateAgent(AgentContainer otherAgentContainer, Position translation) {
        if (otherAgentContainer.getAgentName().equals(selfAgentContainer.getAgentName()) || translationMap.containsKey(otherAgentContainer.getAgentName())) {
            System.out.println("Agent can not authenticate self or previously authenticated agent.");
            return;
        }

        String agentName = otherAgentContainer.getAgentName();
        translationMap.put(agentName, translation);
        containerMap.put(agentName, otherAgentContainer);
        mergeCompleteMap(otherAgentContainer);
    }

    public Position translateToAgent(AgentContainer agentContainer, Position position) {
        return position.subtract(translationMap.getOrDefault(agentContainer.getAgentName(), Position.ZERO));
    }

    /**
     * Merges the full map from the other agent container. This is typically done
     * when two agents initially authenticate each other.
     *
     * @param otherAgentContainer The other agent container to merge from.
     */
    public void mergeCompleteMap(AgentContainer otherAgentContainer) {
        long startTime = System.nanoTime();
        mergeMapPercepts(otherAgentContainer.getAgentName(), otherAgentContainer.getAgentMap().getMapGraph().getCache());
        long deltaTime = (System.nanoTime() - startTime) / 1000000;

        if (deltaTime > 200) {
            LOG.warning("Agent " + selfAgentContainer.getAgentName() + " took " + deltaTime + " to perform a full merge with agent " + otherAgentContainer.getAgentName());
        }

    }


    /**
     * Iterates through all agent maps and pulls the current percepts.
     */
    public void pullMapPerceptsFromAgents() {
        containerMap.values().forEach(agentContainer -> mergeMapPercepts(agentContainer.getAgentName(), agentContainer.getAgentMap().getCurrentPercepts()));
        Message.createAndSendAuthenticatedMessage(selfAgentContainer.getMqSender(), getAuthenticatedAgents(), getTranslationValues());
    }

    /**
     * Merges the provided percepts perceived by all other agents.
     *
     * @param agentName     The name of the agent we are syncing our map with
     * @param mapPerceptMap The updated perceptions received by the agent.
     */
    public void mergeMapPercepts(String agentName, Map<Position, MapPercept> mapPerceptMap) {
        if (agentName == null || mapPerceptMap == null || mapPerceptMap.isEmpty())
            return;

        Position translation = translationMap.getOrDefault(agentName, null);

        if (translation == null)
            return;

        // Get all positions on the other agent map and add to our own knowledge
        List<MapPercept> perceptChunkUpdate = mapPerceptMap.values().stream().map(percept -> percept.copyToAgent(translation.negate())).collect(Collectors.toList());
        selfAgentContainer.getAgentMap().updateMapChunk(perceptChunkUpdate);
    }

    public List<AgentContainer> getAuthenticatedAgents() {
        return new ArrayList<>(containerMap.values());
    }

    public Map<String, Position> getTranslationValues() {
        return Map.copyOf(this.translationMap);
    }
}
