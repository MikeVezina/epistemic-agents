package messages;

import eis.agent.AgentContainer;
import eis.percepts.containers.SharedPerceptContainer;
import map.MapPercept;
import map.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class allows us to transport the information contained within an AgentContainer.
 * Yes this is a terrible way of doing this because lots of stuff is duplicated, but it was a quick fix to the
 * issues I was having with serialization of AgentContainer objects (which result in endless recursive serialization).
 * It was just easier to create a Class that contained only the necessary information.
 */
public class AgentContainerMessage {
    private Position currentLocation;
    private String agentName;
    private Set<Position> attachedBlockPositions;
    private Map<String, Position> authenticatedTeammatePositions;
    private List<MapPercept> currentStepChunks;
    private String team;
    private int vision;
    private int score;
    private long currentStep;

    public AgentContainerMessage(AgentContainer agentContainer) {
        this.agentName = agentContainer.getAgentName();
        this.currentLocation = agentContainer.getCurrentLocation();
        this.attachedBlockPositions = agentContainer.getAttachedPositions();

        this.authenticatedTeammatePositions = new HashMap<>();

        agentContainer.getAgentAuthentication().getAuthenticatedTeammatePositions().forEach((a, v) ->
                this.authenticatedTeammatePositions.put(a.getAgentName(), v));

        this.currentStepChunks = agentContainer.getAgentMap().getCurrentStepChunks();
        this.team = agentContainer.getSharedPerceptContainer().getTeamName();
        this.vision = agentContainer.getSharedPerceptContainer().getVision();
        this.score = agentContainer.getSharedPerceptContainer().getScore();
        this.currentStep = agentContainer.getCurrentStep();
    }

    public Position getCurrentLocation() {
        return currentLocation;
    }

    public String getAgentName() {
        return agentName;
    }

    public Set<Position> getAttachedBlockPositions() {
        return attachedBlockPositions;
    }

    public String getTeam() {
        return team;
    }

    public int getVision() {
        return vision;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Position> getAuthenticatedTeammatePositions() {
        return authenticatedTeammatePositions;
    }

    public List<MapPercept> getCurrentStepChunks() {
        return currentStepChunks;
    }

    public long getCurrentStep() {
        return currentStep;
    }
}
