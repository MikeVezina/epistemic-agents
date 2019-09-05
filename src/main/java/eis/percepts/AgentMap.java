package eis.percepts;

import eis.iilang.Percept;
import eis.percepts.terrain.ForbiddenCell;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AgentMap {
    private static Logger LOG = Logger.getLogger(AgentMap.class.getName());
    private String agent;
    private static int vision = -1;
    private Graph mapKnowledge;
    private ConcurrentMap<Position, MapPercept> currentStepKnowledge;
    private ConcurrentMap<String, Map<Position, MapPercept>> agentUpdates;
    private Map<String, AgentMap> knownAgentMaps;
    private Map<String, Position> translationPositions;

    private Position currentAgentPosition;
    private long lastUpdateStep = -1;

    public AgentMap(String agent) {
        this.agent = agent;
        this.mapKnowledge = new Graph(this);
        this.knownAgentMaps = new HashMap<>();
        this.translationPositions = new HashMap<>();
        this.currentAgentPosition = new Position();
        agentUpdates = new ConcurrentHashMap<>();
    }

    public static void SetVision(int vision) {
        AgentMap.vision = vision;
    }

    public static int GetVision() {
        return vision;
    }

    public String getAgent() {
        return agent;
    }

    public long getLastUpdateStep() {
        return lastUpdateStep;
    }

    public void prepareCurrentStep(long currentStep, Position agentPosition) {
        currentStepKnowledge = new ConcurrentHashMap<>();
        currentAgentPosition = agentPosition;
        lastUpdateStep = currentStep;

        // Generates positions for the current agent's perception
        for (Position p : new Utils.Area(agentPosition, vision)) {
            currentStepKnowledge.put(p, new MapPercept(p, agent, currentStep));

        }

    }

    public Position getCurrentAgentPosition() {
        return currentAgentPosition;
    }

    public void updateThing(Position location, Thing thing)
    {

    }

    public void updateMap(Percept p) {
        if (!Thing.canParse(p) && !Obstacle.canParse(p))
            return;

        // The first two parameters will be X, Y for both obstacle and thing perceptions.
        int x = PerceptUtils.GetNumberParameter(p, 0).intValue();
        int y = PerceptUtils.GetNumberParameter(p, 1).intValue();


        // Create a position for the percept and get the existing MapPercept
        Position curPosition = new Position(x, y);

        // Convert to an absolute position
        curPosition = curPosition.add(currentAgentPosition);

        MapPercept currentMapPercept = currentStepKnowledge.get(curPosition);

        if (currentMapPercept == null) {
            LOG.info("Null: " + curPosition);
        }

        if (Thing.canParse(p)) {
            Thing thing = Thing.ParseThing(p);
            currentMapPercept.setThing(thing);
        } else if (Terrain.canParse(p)) {
            currentMapPercept.setTerrain(Terrain.parseTerrain(p));
        }
    }

    public void agentAuthenticated(String agentName, Position translation, AgentMap agentMap) {
        knownAgentMaps.put(agentName, agentMap);
        translationPositions.put(agentName, translation);

        agentMap.knownAgentMaps.put(this.agent, this);
        agentMap.translationPositions.put(this.agent, translation.negate());

        for (MapPercept percept : getMapKnowledge().values()) {
            MapPercept translatedPercept = percept.copyToAgent(translation);
            agentMap.agentFinalizedPercept(this.agent, translatedPercept);
        }
    }

    private void updateMapLocation(MapPercept updatePercept) {
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

    private void agentFinalizedPercept(String agent, MapPercept updatedPercept) {
        updateMapLocation(updatedPercept);
    }

    public List<MapPercept> getRelativePerceptions(int range) {
        if (range <= 0)
            return new ArrayList<>();

        List<MapPercept> perceptList = new ArrayList<>();

        for (Position p : new Utils.Area(currentAgentPosition, range)) {
            MapPercept relativePercept = new MapPercept(mapKnowledge.get(p));
            relativePercept.setLocation(p.subtract(currentAgentPosition));

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
        return mapKnowledge.getShortestPath(currentAgentPosition, absoluteDestination);
    }

    public Position relativeToAbsoluteLocation(Position relative) {
        return currentAgentPosition.add(relative);
    }

    public Position absoluteToRelativeLocation(Position absolute) {
        return absolute.subtract(currentAgentPosition);
    }

    private MapPercept getTranslatedPercept(String agent, MapPercept percept) {
        Position translation = translationPositions.get(agent);
        return percept.copyToAgent(translation);
    }

    public synchronized void finalizeStep() {
        currentStepKnowledge.entrySet().parallelStream().forEach(e -> {
                    Position currentPosition = e.getKey();
                    MapPercept currentPercept = e.getValue();

                    // Check to see if the cell is forbidden
                    MapPercept lastStepPercept = mapKnowledge.get(e.getKey());

                    if (lastStepPercept != null && lastStepPercept.getTerrain() != null && lastStepPercept.getTerrain() instanceof ForbiddenCell) {
                        // Do nothing.
                    }
                    else
                    {
                        updateMapLocation(e.getValue());
                    }
                }
        );

        //currentStepKnowledge.values().parallelStream().map(p -> p.copyToAgent())

        for (MapPercept percept : currentStepKnowledge.values()) {
            for (AgentMap map : knownAgentMaps.values()) {
                if (percept.getAgentSource().equals(map.agent))
                    continue;
                map.agentFinalizedPercept(this.agent, getTranslatedPercept(map.agent, percept));
            }
        }

        mapKnowledge.redraw();
    }

    public MapPercept getSelfPercept() {
        return mapKnowledge.get(getCurrentAgentPosition());
    }

    public boolean doesBlockAgent(MapPercept percept) {
        return percept == null || percept.isBlocking(getSelfPercept());
    }


    public boolean isAgentBlocked(Direction direction) {
        if(direction == null)
            return false;

        Position dirResult = getCurrentAgentPosition().add(direction.getPosition());
        MapPercept dirPercept = mapKnowledge.get(dirResult);

        return dirPercept == null || dirPercept.isBlocking(getSelfPercept());

    }

    public void addForbidden(Position dirPos) {
        Position absolute = getCurrentAgentPosition().add(dirPos);

        MapPercept percept = mapKnowledge.getOrDefault(absolute, new MapPercept(absolute, this.agent, this.lastUpdateStep));
        percept.setTerrain(new ForbiddenCell(absolute));
        mapKnowledge.put(absolute, percept);

        System.out.println(dirPos);
    }

    public boolean containsEdge(Direction edgeDirection) {
        int edgeScalar = AgentMap.GetVision() + 1;
        Position absolute = getCurrentAgentPosition().add(edgeDirection.multiply(edgeScalar));
        return this.getMapGraph().containsKey(absolute);
    }

    public void processTerrainPerceptions(List<Terrain> mappedPercepts) {
        mappedPercepts.forEach(p -> {
            p.isBlocking();
        });
    }
}
