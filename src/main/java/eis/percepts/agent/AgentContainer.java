package eis.percepts.agent;

import eis.iilang.Percept;
import eis.percepts.handlers.*;
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
}
