package eis.percepts;

import eis.percepts.handlers.AgentLocationPerceptHandler;
import eis.percepts.handlers.PerceptHandler;
import eis.percepts.handlers.TerrainPerceptHandler;
import eis.percepts.handlers.ThingPerceptHandler;
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

    public AgentContainer(String agentName)
    {
        this.agentName = agentName;
        this.agentMap = new AgentMap(agentName);
        this.perceptHandlers = new ArrayList<>();
        this.authenticatedAgents = new HashMap<>();
        this.agentLocation = new AgentLocation(agentName);

        // Add current location listener
        this.agentLocation.addListener(agentMap.getMapGraph());

    }

    private void addPerceptHandlers()
    {
        this.perceptHandlers.add(new AgentLocationPerceptHandler(agentName, agentLocation));
        this.perceptHandlers.add(new TerrainPerceptHandler(agentName, agentMap));
        this.perceptHandlers.add(new ThingPerceptHandler(agentName, agentMap));
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

}
