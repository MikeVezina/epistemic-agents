package eis.percepts.agent;

import eis.iilang.Identifier;
import eis.iilang.Percept;
import eis.listeners.PerceptListener;
import eis.percepts.MapPercept;
import eis.percepts.containers.AgentPerceptContainer;
import utils.Position;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AgentContainer {


    private AgentLocation agentLocation;
    private AgentMap agentMap;
    private AgentAuthentication agentAuthentication;
    private ConcurrentLinkedQueue<PerceptListener> perceptListeners;
    private String agentName;
    private List<Percept> currentStepPercepts;
    private AgentPerceptContainer perceptContainer;
    private Set<Position> attachedBlocks;

    public AgentContainer(String agentName) {
        this.agentName = agentName;
        this.agentLocation = new AgentLocation();
        this.perceptListeners = new ConcurrentLinkedQueue<>();
        this.attachedBlocks = new HashSet<>();
        this.currentStepPercepts = new ArrayList<>();

        this.agentAuthentication = new AgentAuthentication(this);
        this.agentMap = new AgentMap(this);
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

    public AgentAuthentication getAgentAuthentication() {
        return agentAuthentication;
    }

    public String getAgentName() {
        return agentName;
    }

    /**
     * This method needs to be lightweight and should only be responsible for updating the percept container. Do not call any
     * listeners or perform any GUI updates.
     *
     * @param percepts The current step percepts for this agent.
     */
    public synchronized void updatePerceptions(List<Percept> percepts) {
        this.currentStepPercepts = percepts;

        // Create a new percept container for this step.
        perceptContainer = AgentPerceptContainer.parsePercepts(percepts);

        notifyAll(); // Notify any agents that are waiting for perceptions

        handleLastAction();
    }

    private void handleLastAction() {
        // Update the location
        updateLocation();
    }

    private void updateLocation() {
        if (perceptContainer.getLastAction().equals("move") && perceptContainer.getLastActionResult().equals("success")) {
            String directionIdentifier = ((Identifier) perceptContainer.getLastActionParams().get(0)).getValue();
            agentLocation.updateAgentLocation(Utils.DirectionToRelativeLocation(directionIdentifier));
        }
    }

    public synchronized long getCurrentStep() {
        return getPerceptContainer().getSharedPerceptContainer().getStep();
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

    public synchronized AgentPerceptContainer getPerceptContainer() {

        // Wait for percepts if they haven't been set yet.
        if (this.perceptContainer == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return perceptContainer;
    }

    public void attachPerceptListener(PerceptListener perceptListener)
    {
        if(perceptListener == null || perceptListeners.contains(perceptListener))
            return;

        perceptListeners.add(perceptListener);
    }

    public void notifyPerceptsUpdated() {

        perceptListeners.forEach(pL -> pL.perceptsProcessed(this));
    }
}
