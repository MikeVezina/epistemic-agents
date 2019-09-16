package eis.map;

import eis.agent.AgentContainer;
import eis.agent.Rotation;
import eis.messages.Message;
import eis.percepts.terrain.ForbiddenCell;
import eis.percepts.terrain.FreeSpace;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Entity;
import eis.percepts.things.Thing;
import es.usc.citius.hipster.algorithm.ADStarForward;
import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.model.ADStarNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.function.impl.ADStarNodeFactory;
import es.usc.citius.hipster.model.impl.ADStarNodeImpl;
import es.usc.citius.hipster.model.problem.SearchComponents;
import es.usc.citius.hipster.util.examples.RomanianProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AgentMap {
    private static Logger LOG = LoggerFactory.getLogger(AgentMap.class.getName());
    private Graph mapKnowledge;
    private AgentContainer agentContainer;
    private Map<Position, MapPercept> currentPerceptions;
    private List<Position> forbiddenLocations;


    public AgentMap(AgentContainer agentContainer) {
        this.agentContainer = agentContainer;
        forbiddenLocations = new ArrayList<>();
        currentPerceptions = new HashMap<>();
        this.mapKnowledge = new Graph(agentContainer);
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

    public MapPercept getMapPercept(Position absolutePosition) {
        return getMapGraph().get(absolutePosition);
    }

    public List<Rotation> getRotationDirections() {
        List<Rotation> rotations = new ArrayList<>();

        for (Rotation r : Rotation.values()) {
            boolean isBlocked = false;

            for (Position perceptPosition : getAgentContainer().getAttachedPositions()) {
                MapPercept attachedPercept = getMapPercept(getCurrentAgentPosition().add(perceptPosition));

                Position rotatedPosition = getCurrentAgentPosition().add(r.rotate(perceptPosition));
                MapPercept rotatedPercept = getMapPercept(rotatedPosition);

                if (rotatedPercept.isBlocking(attachedPercept)) {
                    isBlocked = true;
                    break;
                }
            }

            if (!isBlocked)
                rotations.add(r);
        }

        return rotations;
    }

    private synchronized List<Position> createADStarNavigation(Position startingPoint, Position destination) {

        Stopwatch stopwatch = Stopwatch.startTiming();

        // Create the search components (starting point, destination, etc.)
        SearchComponents<Double, Position, ?> components = GraphSearchProblem.startingFrom(startingPoint)
                .goalAt(destination)
                .in(mapKnowledge)
                .takeCostsFromEdges()
                .useHeuristicFunction(state -> Math.abs(destination.subtract(state).getDistance()))
                .components();;

        ADStarForward adStarForward = Hipster.createADStar(components);
        Iterator<Node<Void, Position, ? extends ADStarNode<Void, Position, ?, ?>>> iterator = adStarForward.iterator();

        Node<Void, Position, ? extends ADStarNode<Void, Position,?, ?>> node = null;
        do{
            node = iterator.next();
        }while(iterator.hasNext() && !node.state().equals(destination));

        long timedSearch = stopwatch.stopMS();
        System.out.println("Took " + timedSearch + " ms to search: " + node.pathSize());

        // Convert AD Node to Position (aka states)
        stopwatch = Stopwatch.startTiming();
        List<Position> positions = node.path().stream().map(ADStarNode::state).collect(Collectors.toList());

        long timedConversion = stopwatch.stopMS();
        System.out.println("Took " + timedConversion + " ms to convert to positions.");
        return positions;

    }

    public synchronized Map<Position, MapPercept> getCurrentPercepts()
    {
        return currentPerceptions;
    }

    public synchronized void updateMap()
    {
        // Clear list for new percepts.
        currentPerceptions.clear();

        int vision = agentContainer.getSharedPerceptContainer().getVision();
        long currentStep = agentContainer.getSharedPerceptContainer().getStep();
        Position currentAgentPosition = agentContainer.getCurrentLocation();

        List<Thing> thingPerceptions = agentContainer.getAgentPerceptContainer().getThingList();
        List<Terrain> terrainPerceptions = agentContainer.getAgentPerceptContainer().getTerrainList();

        // Create an area around the current position with the current percept visions.
        // All Spaces are automatically filled in with FreeSpace terrain.
        for (Position pos : new Utils.Area(currentAgentPosition, vision)) {
            currentPerceptions.put(pos, new MapPercept(pos, agentContainer, currentStep));
            Terrain defaultTerrain = new FreeSpace(pos.subtract(currentAgentPosition));

            // Handle forbidden locations differently since this is technically a "fake" terrain
            if(forbiddenLocations.contains(pos))
                defaultTerrain = new ForbiddenCell(pos);

            currentPerceptions.get(pos).setTerrain(defaultTerrain);
        }

        for (Thing thing : thingPerceptions) {
            Position absolutePos = currentAgentPosition.add(thing.getPosition());
            MapPercept mapPercept = currentPerceptions.get(absolutePos);

            if (mapPercept == null) {
                LOG.info("Null: " + mapPercept + ". Possibly an invalid vision parameter: " + vision);
            }
            if (mapPercept != null) {
                mapPercept.addThing(thing);
            }
        }

        for (Terrain terrain : terrainPerceptions) {
            Position absolutePos = currentAgentPosition.add(terrain.getPosition());
            MapPercept mapPercept = currentPerceptions.get(absolutePos);

            if (mapPercept == null) {
                LOG.error("Null: " + mapPercept + ". Possibly an invalid vision parameter: " + vision);
            }

            if (mapPercept != null) {
                mapPercept.setTerrain(terrain);
            }
        }

       updateMapChunk(getCurrentPercepts().values());
    }


    /**
     * Updates a chunk of the map.
     *
     * @param mapPerceptChunk The updated perceptions received by the agent.
     */
    public void updateMapChunk(Collection<MapPercept> mapPerceptChunk) {
        if (mapPerceptChunk == null || mapPerceptChunk.isEmpty())
            return;

        // Get all positions on the other agent map and add to our own knowledge
        List<MapPercept> updatedMapChunk = mapPerceptChunk.stream().filter(this::shouldUpdateMapPercept).collect(Collectors.toList());

        // Update our map knowledge
        mapKnowledge.updateChunk(updatedMapChunk);

        // Send percept updates to any consumers.
        Message.createAndSendPerceptMessage(agentContainer.getMqSender(), agentContainer.getAgentLocation(), updatedMapChunk);
    }

    /**
     * Updates a Single map location based on the given MapPercept
     * @param updatePercept
     */
    private boolean shouldUpdateMapPercept(MapPercept updatePercept) {
        MapPercept currentPercept = mapKnowledge.get(updatePercept.getLocation());

        // We want to preserve Forbidden cells, since they are not perceived explicitly.
        if(updatePercept.getTerrain() instanceof ForbiddenCell)
            addForbiddenLocation(absoluteToRelativeLocation(updatePercept.getLocation()));

        // If we dont have a percept at the location, or if the existing information is older, set it.
        return currentPercept == null || currentPercept.getLastStepPerceived() < updatePercept.getLastStepPerceived();
    }

    /**
     * Get the MapPercept object in the relative direction of the agent.
     * @param dir
     * @return
     */
    public MapPercept getRelativePerception(Direction dir) {
        if (dir == null || dir.equals(Direction.NONE))
            return null;
        Position pos = getCurrentAgentPosition().add(dir.getPosition());
        return mapKnowledge.get(pos);
    }


    public Graph getMapGraph() {
        return this.mapKnowledge;
    }

    /**
     * @return
     */
    public List<Position> getNavigationPath(Position absoluteDestination) {
        return createADStarNavigation(getCurrentAgentPosition(), absoluteDestination);
    }

    public Map<Direction, MapPercept> getSurroundingPercepts(MapPercept percept) {
        if (percept == null)
            return null;

        Map<Direction, MapPercept> mapPercepts = new HashMap<>();
        for (Direction direction : Direction.validDirections()) {
            Position absolute = percept.getLocation().add(direction.getPosition());
            MapPercept dirPercept = getMapGraph().get(absolute);
            mapPercepts.put(direction, dirPercept);
        }

        return mapPercepts;
    }

    /**
     * Method overload for navigating to thing.
     *
     * @param type
     * @param details
     * @return
     */
    public List<Position> getNavigationPath(String type, String details) {
        // It would be good to sort these by how recent the perceptions are, and the distance.
        MapPercept percept = mapKnowledge.getCache().getCachedThingList().stream().filter(p -> p.hasThing(type, details)).findAny().orElse(null);

        if (percept == null)
            return null;

        List<Position> shortestPath = createADStarNavigation(getCurrentAgentPosition(), percept.getLocation());


        if (shortestPath != null) {
            shortestPath.removeIf(p -> p.equals(percept.getLocation()));
            return shortestPath;
        }

        for (Map.Entry<Direction, MapPercept> surroundingPercepts : getSurroundingPercepts(percept).entrySet()) {
            shortestPath = mapKnowledge.getShortestPath(getCurrentAgentPosition(), surroundingPercepts.getValue().getLocation());

            if (shortestPath != null)
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

    public Entity getSelfEntity() {
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

        if (dirPercept == null)
            return false;

        return !getAgentContainer().getAttachedPositions().contains(direction.getPosition()) && dirPercept.isBlocking(getSelfPercept());
    }

    public void addForbiddenLocation(Position position) {
        Position absolute = relativeToAbsoluteLocation(position);

        if(forbiddenLocations.contains(absolute))
            return;

        this.forbiddenLocations.add(absolute);
    }

    public boolean areAttachmentsBlocked(Direction direction) {
        if (direction == null || !getAgentContainer().hasAttachedPercepts())
            return false;

        Position dirResult = getCurrentAgentPosition().add(direction.getPosition());
        MapPercept dirPercept = mapKnowledge.get(dirResult);

        if (dirPercept == null)
            return false;

        for (Position relative : getAgentContainer().getAttachedPositions()) {
            if (isAttachedThingBlocked(relative, direction))
                return true;
        }

        return false;
    }

    public boolean canAgentMove(Direction direction) {
       return !isAgentBlocked(direction) && !areAttachmentsBlocked(direction);
    }

    public boolean isAttachedThingBlocked(Position attachedPosition, Direction direction) {
        if (direction == null || attachedPosition == null)
            return false;

        Position attachedPerceptPosition = getCurrentAgentPosition().add(attachedPosition);
        MapPercept attachedPercept = mapKnowledge.get(attachedPerceptPosition);

        Position nextPosition = attachedPerceptPosition.add(direction.getPosition());
        MapPercept nextPercept = mapKnowledge.get(nextPosition);

        return nextPercept != null && (nextPercept.hasBlock() || !getSelfPercept().equals(nextPercept)) && nextPercept.isBlocking(attachedPercept);
    }

    public boolean containsEdge(Direction edgeDirection) {
        int vision = agentContainer.getAgentPerceptContainer().getSharedPerceptContainer().getVision();
        if (vision == -1)
            return false;

        int edgeScalar = vision + 1;
        Position absolute = getCurrentAgentPosition().add(edgeDirection.multiply(edgeScalar));
        return this.getMapGraph().containsKey(absolute);
    }

    public List<MapPercept> getSortedGoalPercepts(Predicate<MapPercept> filter) {
        return mapKnowledge.getCache().getCachedTerrain().stream()
                .filter(p -> p.getTerrain() instanceof Goal)
                .filter(filter)
                .sorted((g1, g2) -> (int) (g1.getLocation().subtract(getCurrentAgentPosition()).getDistance() - g2.getLocation().subtract(getCurrentAgentPosition()).getDistance()))
                .collect(Collectors.toList());
    }
}
