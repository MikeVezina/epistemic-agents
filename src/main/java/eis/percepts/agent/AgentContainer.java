package eis.percepts.agent;

import eis.EISAdapter;
import eis.iilang.Identifier;
import eis.iilang.Percept;
import eis.percepts.MapPercept;
import eis.percepts.containers.PerceptContainer;
import massim.eismassim.EnvironmentInterface;
import utils.PerceptUtils;
import utils.Position;
import utils.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AgentContainer {


    private AgentLocation agentLocation;
    private AgentMap agentMap;
    private ConcurrentMap<String, AgentContainer> authenticatedAgents;
    private String agentName;
    private List<Percept> currentStepPercepts;
    private PerceptContainer perceptContainer;
    private Set<Position> attachedBlocks;

    public AgentContainer(String agentName) {
        this.agentName = agentName;
        this.authenticatedAgents = new ConcurrentHashMap<>();
        this.currentStepPercepts = new ArrayList<>();
        this.agentLocation = new AgentLocation(agentName);

        this.agentMap = new AgentMap(this);

        // Add current location listener
        this.agentLocation.addListener(agentMap.getMapGraph());
        this.attachedBlocks = new HashSet<>();

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

    public synchronized void updatePerceptions(List<Percept> percepts) {
        this.currentStepPercepts = percepts;

        // Create a new percept container for this step.
        perceptContainer = PerceptContainer.parsePercepts(percepts);
        updateLocation();

        notify(); // Notify any agents that are waiting for perceptions

        System.out.println(agentLocation.getCurrentLocation());
        agentMap.getMapGraph().redraw();
    }

    private void updateLocation() {
        if (perceptContainer.getLastAction().equals("move") && perceptContainer.getLastActionResult().equals("success")) {
            String directionIdentifier = ((Identifier) perceptContainer.getLastActionParams().get(0)).getValue();
            agentLocation.updateAgentLocation(Utils.DirectionToRelativeLocation(directionIdentifier));
        }

    }

    public synchronized long getCurrentStep() {
        return getPerceptContainer().getStep();
    }

    public synchronized List<Percept> getCurrentPerceptions() {
        if (this.currentStepPercepts == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return this.currentStepPercepts;
    }

    public void attachBlock(Position position) {
        attachedBlocks.add(position);
    }

    public boolean hasAttachedPercepts() {
        return !attachedBlocks.isEmpty();
    }

    public Set<Position> getAttachedPositions() {
        return attachedBlocks;
    }

    public void rotate(Rotation rotation) {
        List<Position> rotatedAttachments = new ArrayList<>();
        for (Position p : attachedBlocks) {
            rotatedAttachments.add(rotation.rotate(p));
        }
        attachedBlocks.clear();
        attachedBlocks.addAll(rotatedAttachments);
    }


    public boolean isAttachedPercept(MapPercept mapPercept) {
        if (mapPercept == null)
            return false;

        Position relativePos = mapPercept.getLocation().subtract(getCurrentLocation());
        return mapPercept.hasBlock() && attachedBlocks.contains(relativePos);
    }

    public void detachBlock(Position position) {
        if (position == null)
            return;

        attachedBlocks.remove(position);
    }

    public void taskSubmitted() {
        attachedBlocks.clear();
    }

    public synchronized PerceptContainer getPerceptContainer() {
        if (this.perceptContainer == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return perceptContainer;
    }
}
