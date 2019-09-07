package eis.percepts.agent;

import eis.EISAdapter;
import eis.iilang.Percept;
import eis.percepts.MapPercept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AgentMapSynchronization {
    private static AgentMapSynchronization agentMapSynchronization;
    private ConcurrentMap<String, List<MapPercept>> currentAgentPercepts;
    private ConcurrentMap<String, AgentContainer> currentAgentContainers;

    private AgentMapSynchronization()
    {
        this.currentAgentPercepts = new ConcurrentHashMap<>();
        this.currentAgentContainers = new ConcurrentHashMap<>();
    }

    synchronized void setUpdateFlag(AgentContainer agentContainer, List<MapPercept> percepts)
    {
        if(currentAgentPercepts.containsKey(agentContainer.getAgentName()))
            return;

        currentAgentContainers.put(agentContainer.getAgentName(), agentContainer);
        currentAgentPercepts.put(agentContainer.getAgentName(), percepts);

        if(currentAgentPercepts.size() == EISAdapter.getSingleton().getAgentContainers().size())
            syncAgents();

    }

    synchronized void syncAgents()
    {
        currentAgentContainers.values().forEach(agentContainer -> {
            agentContainer.getAgentMap().getAgentAuthentication().pullAgentUpdates();
        });

        currentAgentPercepts.clear();
        currentAgentContainers.clear();
    }

    public static AgentMapSynchronization getSyncInstance()
    {
        if(agentMapSynchronization == null)
            agentMapSynchronization = new AgentMapSynchronization();

        return agentMapSynchronization;
    }
}
