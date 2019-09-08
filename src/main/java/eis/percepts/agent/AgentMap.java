package eis.percepts.agent;

import eis.percepts.MapPercept;
import eis.percepts.terrain.ForbiddenCell;
import eis.percepts.terrain.Goal;
import eis.percepts.things.Dispenser;
import eis.percepts.things.Entity;
import utils.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public MapPercept getMapPercept(Position absolutePosition)
    {
        return getMapGraph().get(absolutePosition);
    }

    public List<Rotation> getRotationDirections()
    {
        List<Rotation> rotations = new ArrayList<>();

        for(Rotation r : Rotation.values())
        {
            boolean isBlocked = false;

            for(Position perceptPosition : getAgentContainer().getAttachedPositions())
            {
                MapPercept attachedPercept = getMapPercept(perceptPosition);

                Position rotatedPosition = r.rotate(perceptPosition);
                MapPercept rotatedPercept = getMapPercept(rotatedPosition);

                if(rotatedPercept.isBlocking(attachedPercept)) {
                    isBlocked = true;
                    break;
                }
            }

            if(!isBlocked)
                rotations.add(r);
        }

        return rotations;
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

    public MapPercept getRelativePerception(Direction dir) {
        if (dir == null || dir.equals(Direction.NONE))
            return null;
        Position pos = getCurrentAgentPosition().add(dir.getPosition());
        return mapKnowledge.get(pos);
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

    public Map<Direction, MapPercept> getSurroundingPercepts(MapPercept percept)
    {
        if(percept == null)
            return null;

        Map<Direction, MapPercept> mapPercepts = new HashMap<>();
        for(Direction direction : Direction.validDirections())
        {
            Position absolute = getCurrentAgentPosition().add(direction.getPosition());
            MapPercept dirPercept = getMapGraph().get(absolute);
            mapPercepts.put(direction, dirPercept);
        }

        return mapPercepts;
    }

    /**
     * Method overload for navigating to thing.
     * @param type
     * @param details
     * @return
     */
    public List<Position> getNavigationPath(String type, String details) {
        // It would be good to sort these by how recent the perceptions are, and the distance.
        MapPercept percept = mapKnowledge.values().stream().filter(p -> p.hasThing(type, details)).findAny().orElse(null);

        if(percept == null)
            return null;


        // We typically shouldn't navigate directly on top of the thing
        List<Position> shortestPath = mapKnowledge.getShortestPath(getCurrentAgentPosition(), percept.getLocation());


        if(shortestPath != null) {
            shortestPath.removeIf(p -> p.equals(percept.getLocation()));
            return shortestPath;
        }

        for(Map.Entry<Direction, MapPercept> surroundingPercepts : getSurroundingPercepts(percept).entrySet())
        {
            shortestPath = mapKnowledge.getShortestPath(getCurrentAgentPosition(), surroundingPercepts.getValue().getLocation());

            if(shortestPath != null)
                return shortestPath;
        }

        return shortestPath;
    }

    public Position relativeToAbsoluteLocation(Position relative) {
        return getCurrentAgentPosition().add(relative);
    }

    public Position absoluteToRelativeLocation(Position absolute) {
        return absolute.subtract(getCurrentAgentPosition());
    }

    public MapPercept getSelfPercept() {
        return mapKnowledge.get(getCurrentAgentPosition());
    }

    public Entity getSelfEntity()
    {
        return getSelfPercept().getThingList().stream().filter(t -> t instanceof Entity).map(t -> (Entity) t).findAny().orElse(null);
    }


    public boolean doesBlockAgent(MapPercept percept) {
        return percept == null || (percept.isBlocking(getSelfPercept()));
    }


    public boolean isAgentBlocked(Direction direction) {
        if (direction == null)
            return false;

        Position dirResult = getCurrentAgentPosition().add(direction.getPosition());
        MapPercept dirPercept = mapKnowledge.get(dirResult);

        if(dirPercept == null)
            return false;

        if(!getAgentContainer().hasAttachedPercepts())
            return dirPercept.isBlocking(getSelfPercept());

        boolean isSelfBlocked = false;

        // If we dont have an attached block in this direction, check if we can move
        if(!getAgentContainer().getAttachedPositions().contains(direction.getPosition()))
            isSelfBlocked = dirPercept.isBlocking(getSelfPercept());

        boolean isAttachedBlocked = false;
        for(Position relative : getAgentContainer().getAttachedPositions())
        {
            if(isAttachedThingBlocked(relative, direction))
            {
                isAttachedBlocked = true;
                break;
            }
        }

        return isSelfBlocked || isAttachedBlocked;

    }

    public boolean isAttachedThingBlocked(Position attachedPosition, Direction direction) {
        if (direction == null || attachedPosition == null)
            return false;

        Position attachedPerceptPosition = getCurrentAgentPosition().add(attachedPosition);
        MapPercept attachedPercept = mapKnowledge.get(attachedPerceptPosition);

        Position nextPosition = attachedPerceptPosition.add(direction.getPosition());
        MapPercept nextPercept = mapKnowledge.get(nextPosition);

        return nextPercept != null && !getSelfPercept().equals(nextPercept) && nextPercept.isBlocking(attachedPercept);
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

    public List<MapPercept> getSortedGoalPercepts(Predicate<MapPercept> filter)
    {
        return mapKnowledge.values().parallelStream()
                .filter(p -> p.getTerrain() instanceof Goal)
                .filter(filter)
                .sorted((g1, g2) -> (int) (g1.getLocation().subtract(getCurrentAgentPosition()).getDistance() - g2.getLocation().subtract(getCurrentAgentPosition()).getDistance()))
                .collect(Collectors.toList());
    }
}
