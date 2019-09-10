package eis.percepts.agent;

import eis.listeners.PerceptListener;
import eis.percepts.MapPercept;
import eis.percepts.terrain.ForbiddenCell;
import eis.percepts.terrain.FreeSpace;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.Graph;
import utils.Position;
import utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class AgentAuthentication implements PerceptListener {
    private static final Logger LOG = Logger.getLogger(AgentAuthentication.class.getName());
    private ConcurrentMap<String, AgentContainer> agentContainerMap;
    private ConcurrentMap<String, Position> translationMap;
    private AgentContainer selfAgentContainer;
    private ConcurrentMap<Position, MapPercept> currentPerceptions;

    public AgentAuthentication(AgentContainer selfAgentContainer) {
        this.selfAgentContainer = selfAgentContainer;
        this.translationMap = new ConcurrentHashMap<>();
        this.agentContainerMap = new ConcurrentHashMap<>();
        selfAgentContainer.attachPerceptListener(this);
    }

    public void authenticateAgent(AgentContainer otherAgentContainer, Position translation) {
        if (otherAgentContainer.getAgentName().equals(selfAgentContainer.getAgentName()) || agentContainerMap.containsKey(otherAgentContainer.getAgentName())) {
            System.out.println("Agent can not authenticate self or previously authenticated agent.");
            return;
        }

        String agentName = otherAgentContainer.getAgentName();
        agentContainerMap.put(agentName, otherAgentContainer);
        translationMap.put(agentName, translation);

    }

    public synchronized void forceMapSync(String agentName) {
        AgentContainer agentContainer = agentContainerMap.getOrDefault(agentName, null);

        if (agentContainer == null)
            return;

        syncMap(agentContainer);
    }

    public Position translateToAgent(AgentContainer agentContainer, Position position)
    {
        return position.subtract(translationMap.getOrDefault(agentContainer.getAgentName(), Position.ZERO));
    }


    private void syncMap(AgentContainer otherAgentContainer)
    {
        Position translation = translationMap.getOrDefault(otherAgentContainer.getAgentName(), null);

        if (translation == null)
            return;

        // Get all positions on the other agent map and add to our own knowledge
        otherAgentContainer.getAgentMap().getMapGraph().forEach((position, percept) -> {
            // Copy to self (negate translation)
            MapPercept translatedPercept = percept.copyToAgent(translation.negate());
            selfAgentContainer.getAgentMap().updateMapLocation(translatedPercept);
        });
    }

    private synchronized void updateFromAgent(String agentName, Map<Position, MapPercept> mapPerceptMap) {
        AgentContainer agentContainer = agentContainerMap.getOrDefault(agentName, null);
        Position translation = translationMap.getOrDefault(agentName, null);

        if (agentContainer == null || translation == null)
            return;

        for(Map.Entry<Position, MapPercept> mapPerceptEntry : mapPerceptMap.entrySet())
        {
            MapPercept translatedPercept = mapPerceptEntry.getValue().copyToAgent(translation.negate());
            selfAgentContainer.getAgentMap().updateMapLocation(translatedPercept);
        }
    }


    @Override
    public synchronized void perceptsProcessed(AgentContainer agentContainer) {
        int vision = agentContainer.getPerceptContainer().getSharedPerceptContainer().getVision();

        String agentName = agentContainer.getAgentName();
        long currentStep = agentContainer.getPerceptContainer().getSharedPerceptContainer().getStep();

        Position currentAgentPosition = agentContainer.getCurrentLocation();

        List<Thing> thingPerceptions = agentContainer.getPerceptContainer().getThingList();
        List<Terrain> terrainPerceptions = agentContainer.getPerceptContainer().getTerrainList();

        currentPerceptions = new ConcurrentHashMap<>();

        for (Position pos : new Utils.Area(currentAgentPosition, vision)) {
            currentPerceptions.put(pos, new MapPercept(pos, agentName, currentStep));
            currentPerceptions.get(pos).setTerrain(new FreeSpace(pos.subtract(currentAgentPosition)));
        }

        for (Thing thing : thingPerceptions) {
            Position absolutePos = currentAgentPosition.add(thing.getPosition());
            MapPercept mapPercept = currentPerceptions.get(absolutePos);

            if (mapPercept == null) {
                LOG.info("Null: " + mapPercept);
            }
            if (mapPercept != null) {
                mapPercept.addThing(thing);
            }
        }

        for (Terrain terrain : terrainPerceptions) {
            Position absolutePos = currentAgentPosition.add(terrain.getPosition());
            MapPercept mapPercept = currentPerceptions.get(absolutePos);

            if (mapPercept == null) {
                LOG.info("Null: " + mapPercept);
            }

            if (mapPercept != null) {
                mapPercept.setTerrain(terrain);
            }
        }

        Graph mapKnowledge = selfAgentContainer.getAgentMap().getMapGraph();

        currentPerceptions.forEach((key, value) -> {
            // Check to see if the cell is forbidden
            MapPercept lastStepPercept = mapKnowledge.get(key);

            if (lastStepPercept != null && lastStepPercept.getTerrain() != null && lastStepPercept.getTerrain() instanceof ForbiddenCell) {
                // Do nothing.
            } else {
                selfAgentContainer.getAgentMap().updateMapLocation(value);
            }
        });

        AgentMapSynchronization.getSyncInstance().setUpdateFlag(selfAgentContainer, List.copyOf(currentPerceptions.values()));
    }

    public Map<Position, MapPercept> getCurrentPerceptions() {
        return Collections.unmodifiableMap(currentPerceptions);
    }

    public void pullAgentUpdates() {
        for(AgentContainer container : agentContainerMap.values())
        {
            Position translation = translationMap.getOrDefault(container.getAgentName(), null);

            if(translation == null)
                return;

            updateFromAgent(container.getAgentName(), getCurrentPerceptions());
        }
    }
}
