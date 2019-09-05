package eis.percepts;

import eis.iilang.Percept;
import eis.percepts.handlers.*;
import utils.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentContainer {

    private List<PerceptHandler> perceptHandlers;
    private AgentLocation agentLocation;
    private AgentMap agentMap;
    private Map<String, AgentContainer> authenticatedAgents;
    private String agentName;
    private long lastUpdateStep;
    private List<Percept> currentStepPercepts;

    public AgentContainer(String agentName)
    {
        this.agentName = agentName;
        this.agentMap = new AgentMap(agentName);
        this.perceptHandlers = new ArrayList<>();
        this.authenticatedAgents = new HashMap<>();
        this.agentLocation = new AgentLocation(agentName);

        // Add current location listener
        this.agentLocation.addListener(agentMap.getMapGraph());

        addPerceptHandlers();

    }

    private void addPerceptHandlers()
    {
        this.perceptHandlers.add(new AgentLocationPerceptHandler(agentName, agentLocation));
        this.perceptHandlers.add(new TerrainPerceptHandler(agentName, agentMap));
        this.perceptHandlers.add(new ThingPerceptHandler(agentName, agentMap));
        this.perceptHandlers.add(new AgentInfoPerceptHandler(agentName));
    }

    public List<PerceptHandler> getPerceptHandlers() {
        return perceptHandlers;
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
        perceptHandlers.forEach(h -> h.prepareStep(step));

        percepts.parallelStream().forEach(p -> {
            perceptHandlers.forEach(h -> h.handlePercept(p));
        });

        // Process new percepts
        perceptHandlers.forEach(PerceptHandler::processPercepts);

        // getAgentMap().updateMap(p);
    }

}
