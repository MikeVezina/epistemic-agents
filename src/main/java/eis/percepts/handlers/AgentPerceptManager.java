package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.listeners.PerceptListener;
import eis.percepts.agent.AgentContainer;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AgentPerceptManager {
    private Queue<PerceptHandler> perceptHandlers;
    private AgentContainer agentContainer;
    private Queue<PerceptListener> perceptListeners;

    private AgentLocationPerceptHandler agentLocationPerceptHandler;
    private TerrainPerceptHandler terrainPerceptHandler;
    private ThingPerceptHandler thingPerceptHandler;
    private AgentInfoPerceptHandler agentInfoPerceptHandler;


    public AgentPerceptManager(AgentContainer container) {
        this.agentContainer = container;
        perceptHandlers = new LinkedList<>();
        perceptListeners = new LinkedList<>();

        agentLocationPerceptHandler = new AgentLocationPerceptHandler(agentContainer.getAgentName(), agentContainer.getAgentLocation());
        terrainPerceptHandler = new TerrainPerceptHandler(agentContainer.getAgentName(), agentContainer.getAgentMap());
        thingPerceptHandler = new ThingPerceptHandler(agentContainer.getAgentName(), agentContainer.getAgentMap());
        agentInfoPerceptHandler = new AgentInfoPerceptHandler(agentContainer.getAgentName());


        addPerceptHandlers();

    }

    public void addPerceptListener(PerceptListener listener)
    {
        if(listener == null || perceptListeners.contains(listener))
            return;

        perceptListeners.add(listener);
    }

    private void addPerceptHandlers()
    {
        // All handlers are called in this order
        this.perceptHandlers.add(agentLocationPerceptHandler);
        this.perceptHandlers.add(terrainPerceptHandler);
        this.perceptHandlers.add(thingPerceptHandler);
        this.perceptHandlers.add(agentInfoPerceptHandler);
    }

    public void updatePerceptions(long step, List<Percept> percepts) {
        perceptHandlers.forEach(h -> h.prepareStep(step));

        percepts.forEach(p -> {
            perceptHandlers.forEach(h -> h.handlePercept(p));
        });

        // Process new percepts
        perceptHandlers.forEach(PerceptHandler::perceptProcessingFinished);

        for(PerceptListener pL : perceptListeners)
            pL.perceptsProcessed(this);
    }

    public AgentContainer getAgentContainer() {
        return agentContainer;
    }

    public Queue<PerceptListener> getPerceptListeners() {
        return perceptListeners;
    }

    public AgentLocationPerceptHandler getAgentLocationPerceptHandler() {
        return agentLocationPerceptHandler;
    }

    public TerrainPerceptHandler getTerrainPerceptHandler() {
        return terrainPerceptHandler;
    }

    public ThingPerceptHandler getThingPerceptHandler() {
        return thingPerceptHandler;
    }

    public AgentInfoPerceptHandler getAgentInfoPerceptHandler() {
        return agentInfoPerceptHandler;
    }
}
