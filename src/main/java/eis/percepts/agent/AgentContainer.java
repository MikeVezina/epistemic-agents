package eis.percepts.agent;

import eis.iilang.Percept;
import eis.percepts.MapPercept;
import eis.percepts.handlers.*;
import eis.percepts.things.Block;
import utils.Position;

import java.util.*;

public class AgentContainer {


    private AgentLocation agentLocation;
    private AgentMap agentMap;
    private Map<String, AgentContainer> authenticatedAgents;
    private String agentName;
    private long currentStep;
    private List<Percept> currentStepPercepts;
    private AgentPerceptManager perceptManager;
    private Set<Position> attachedBlocks;

    public AgentContainer(String agentName)
    {
        this.agentName = agentName;
        this.authenticatedAgents = new HashMap<>();
        this.currentStepPercepts = new ArrayList<>();
        this.agentLocation = new AgentLocation(agentName);

        this.agentMap = new AgentMap(this);
        this.perceptManager = new AgentPerceptManager(this);

        perceptManager.addPerceptListener(agentMap.getAgentAuthentication());

        // Add current location listener
        this.agentLocation.addListener(agentMap.getMapGraph());
        this.attachedBlocks = new HashSet<>();

    }



    public AgentPerceptManager getPerceptManager() {
        return perceptManager;
    }

    public AgentLocation getAgentLocation() {
        return agentLocation;
    }

    public Position getCurrentLocation() {
        return agentLocation.getCurrentLocation();
    }

    public AgentMap getAgentMap() {
        return agentMap;
    }

    public Map<String, AgentContainer> getAuthenticatedAgents() {
        return authenticatedAgents;
    }

    public String getAgentName() {
        return agentName;
    }

    public void updatePerceptions(long step, List<Percept> percepts)
    {
        this.currentStep = step;
        this.currentStepPercepts = percepts;
        perceptManager.updatePerceptions(step, currentStepPercepts);
        agentMap.getMapGraph().redraw();
    }

    public long getCurrentStep() {
        return currentStep;
    }

    public List<Percept> getCurrentPerceptions() {
        return this.currentStepPercepts;
    }

    public void attachBlock(Position position) {
        attachedBlocks.add(position);
    }

    public boolean hasAttachedPercepts()
    {
        return !attachedBlocks.isEmpty();
    }

    public Set<Position> getAttachedPositions()
    {
        return attachedBlocks;
    }

    public void rotate(Rotation rotation)
    {
        List<Position> rotatedAttachments = new ArrayList<>();
        for(Position p : attachedBlocks)
        {
            rotatedAttachments.add(rotation.rotate(p));
        }
        attachedBlocks.clear();
        attachedBlocks.addAll(rotatedAttachments);
    }


    public boolean isAttachedPercept(MapPercept mapPercept) {
        if(mapPercept == null)
            return false;

        Position relativePos = mapPercept.getLocation().subtract(getCurrentLocation());
        return mapPercept.hasBlock() && attachedBlocks.contains(relativePos);
    }

    public void detachBlock(Position position) {
        if(position == null)
            return;

        attachedBlocks.remove(position);
    }
}
