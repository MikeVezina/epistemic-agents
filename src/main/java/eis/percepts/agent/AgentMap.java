package eis.percepts.agent;

import eis.percepts.MapPercept;
import eis.percepts.terrain.ForbiddenCell;
import utils.*;

import java.util.*;
import java.util.logging.Logger;

public class AgentMap {
    private static Logger LOG = Logger.getLogger(AgentMap.class.getName());
    private Graph mapKnowledge;
    private AgentAuthentication agentAuthentication;
    private AgentContainer agentContainer;


    public AgentMap(AgentContainer agentContainer) {
        this.agentContainer = agentContainer;
        this.agentAuthentication = new AgentAuthentication(agentContainer);
        this.mapKnowledge = new Graph(this);
    }

    public AgentContainer getAgentContainer() {
        return agentContainer;
    }

    public String getAgentName() {
        return agentContainer.getAgentName();
    }

    public Position getCurrentAgentPosition() {
        return agentContainer.getCurrentLocation();
    }


    public void agentAuthenticated(AgentContainer agentContainer, Position translation) {
        agentAuthentication.authenticateAgent(agentContainer, translation);
        agentAuthentication.forceMapSync(agentContainer.getAgentName());
    }

    public AgentAuthentication getAgentAuthentication() {
        return agentAuthentication;
    }

    void updateMapLocation(MapPercept updatePercept) {
        MapPercept currentPercept = mapKnowledge.getOrDefault(updatePercept.getLocation(), null);

        // If we dont have a percept at the location, set it.
        if (currentPercept == null) {
            mapKnowledge.put(updatePercept.getLocation(), updatePercept);
            return;
        }

        // If we do have a perception at the location, but ours is older, then update/overwrite it.
        if (currentPercept.getLastStepPerceived() < updatePercept.getLastStepPerceived())
            mapKnowledge.put(updatePercept.getLocation(), updatePercept);
    }

    public List<MapPercept> getRelativePerceptions(int range) {
        if (range <= 0)
            return new ArrayList<>();

        List<MapPercept> perceptList = new ArrayList<>();

        for (Position p : new Utils.Area(getCurrentAgentPosition(), range)) {
            MapPercept relativePercept = new MapPercept(mapKnowledge.get(p));
            relativePercept.setLocation(p.subtract(getCurrentAgentPosition()));

            perceptList.add(mapKnowledge.get(p));
        }

        return perceptList;
    }

    private Map<Position, MapPercept> getMapKnowledge() {
        return Collections.unmodifiableMap(mapKnowledge);
    }

    public Graph getMapGraph() {
        return this.mapKnowledge;
    }

    /**
     * @return
     */
    public List<Position> getNavigationPath(Position absoluteDestination) {
        return mapKnowledge.getShortestPath(getCurrentAgentPosition(), absoluteDestination);
    }

    public Position relativeToAbsoluteLocation(Position relative) {
        return getCurrentAgentPosition().add(relative);
    }

    public Position absoluteToRelativeLocation(Position absolute) {
        return absolute.subtract(getCurrentAgentPosition());
    }

    public synchronized void finalizeStep() {
//        currentStepKnowledge.entrySet().parallelStream().forEach(e -> {
//                    Position currentPosition = e.getKey();
//                    MapPercept currentPercept = e.getValue();
//
//                    // Check to see if the cell is forbidden
//                    MapPercept lastStepPercept = mapKnowledge.get(e.getKey());
//
//                    if (lastStepPercept != null && lastStepPercept.getTerrain() != null && lastStepPercept.getTerrain() instanceof ForbiddenCell) {
//                        // Do nothing.
//                    } else {
//                        updateMapLocation(e.getValue());
//                    }
//                }
//        );
//
//        //currentStepKnowledge.values().parallelStream().map(p -> p.copyToAgent())
//
//        for (MapPercept percept : currentStepKnowledge.values()) {
//            for (AgentMap map : knownAgentMaps.values()) {
//                if (percept.getAgentSource().equals(map.agent))
//                    continue;
//                map.agentFinalizedPercept(this.agent, getTranslatedPercept(map.agent, percept));
//            }
//        }
//
//        mapKnowledge.redraw();
    }

    public MapPercept getSelfPercept() {
        return mapKnowledge.get(getCurrentAgentPosition());
    }

    public boolean doesBlockAgent(MapPercept percept) {
        return percept == null || percept.isBlocking(getSelfPercept());
    }


    public boolean isAgentBlocked(Direction direction) {
        if (direction == null)
            return false;

        Position dirResult = getCurrentAgentPosition().add(direction.getPosition());
        MapPercept dirPercept = mapKnowledge.get(dirResult);

        return dirPercept == null || dirPercept.isBlocking(getSelfPercept());

    }

    public void addForbidden(Position dirPos) {
        Position absolute = getCurrentAgentPosition().add(dirPos);

        MapPercept percept = mapKnowledge.getOrDefault(absolute, new MapPercept(absolute, this.getAgentName(), agentContainer.getCurrentStep()));
        percept.setTerrain(new ForbiddenCell(absolute));
        mapKnowledge.put(absolute, percept);

        System.out.println(dirPos);
    }

    public boolean containsEdge(Direction edgeDirection) {
        int vision = StaticInfo.getInstance().getVision();
        if (vision == -1)
            return false;

        int edgeScalar = vision + 1;
        Position absolute = getCurrentAgentPosition().add(edgeDirection.multiply(edgeScalar));
        return this.getMapGraph().containsKey(absolute);
    }



}
