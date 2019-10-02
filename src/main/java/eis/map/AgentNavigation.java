package eis.map;

import eis.agent.AgentContainer;
import eis.agent.Rotation;
import messages.Message;
import es.usc.citius.hipster.algorithm.ADStarForward;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.model.ADStarNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.problem.SearchComponents;
import utils.Stopwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AgentNavigation {
    private AgentContainer agentContainer;

    public AgentNavigation(AgentContainer agentContainer)
    {
        this.agentContainer = agentContainer;
    }

    private Graph getMapGraph()
    {
        return agentContainer.getAgentMap().getMapGraph();
    }


    public List<Rotation> getRotationDirections() {
        List<Rotation> rotations = new ArrayList<>();

        for (Rotation r : Rotation.values()) {
            boolean isBlocked = false;

            for (Position perceptPosition : agentContainer.getAttachedPositions()) {
                MapPercept attachedPercept = agentContainer.getAgentMap().getMapPercept(agentContainer.getCurrentLocation().add(perceptPosition));

                Position rotatedPosition = agentContainer.getCurrentLocation().add(r.rotate(perceptPosition));
                MapPercept rotatedPercept = agentContainer.getAgentMap().getMapPercept(rotatedPosition);

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

    /**
     * @return
     */
    public synchronized List<Position> getNavigationPath(Position absoluteDestination) {
        return createADStarNavigation(agentContainer.getCurrentLocation(), absoluteDestination);
    }

    /**
     * Method overload for navigating to thing.
     *
     * @param type
     * @param details
     * @return
     */
    public synchronized List<Position> getNavigationPath(String type, String details) {
        // It would be good to sort these by how recent the perceptions are, and the distance.
        MapPercept percept = getMapGraph().getCache().getCachedThingList().stream().filter(p -> p.hasThing(type, details)).findAny().orElse(null);

        if (percept == null)
            return null;

        List<Position> shortestPath = createADStarNavigation(agentContainer.getCurrentLocation(), percept.getLocation());


        if (shortestPath != null) {
            shortestPath.removeIf(p -> p.equals(percept.getLocation()));
            return shortestPath;
        }

        for (Map.Entry<Direction, MapPercept> surroundingPercepts : agentContainer.getAgentMap().getSurroundingPercepts(percept).entrySet()) {
            shortestPath = getMapGraph().getShortestPath(agentContainer.getCurrentLocation(), surroundingPercepts.getValue().getLocation());

            if (shortestPath != null)
                return shortestPath;
        }

        return shortestPath;
    }

    public boolean areAttachmentsBlocked(Direction direction) {
        if (direction == null || !agentContainer.hasAttachedPercepts())
            return false;

        Position dirResult = agentContainer.getCurrentLocation().add(direction.getPosition());
        MapPercept dirPercept = getMapGraph().get(dirResult);

        if (dirPercept == null)
            return false;

        for (Position relative : agentContainer.getAttachedPositions()) {
            if (isAttachedThingBlocked(relative, direction))
                return true;
        }

        return false;
    }

    public boolean canAgentMove(Direction direction) {
        return !agentContainer.getAgentMap().isAgentBlocked(direction) && !areAttachmentsBlocked(direction);
    }

    public boolean isAttachedThingBlocked(Position attachedPosition, Direction direction) {
        if (direction == null || attachedPosition == null)
            return false;

        Position attachedPerceptPosition = agentContainer.getCurrentLocation().add(attachedPosition);
        MapPercept attachedPercept = getMapGraph().get(attachedPerceptPosition);

        Position nextPosition = attachedPerceptPosition.add(direction.getPosition());
        MapPercept nextPercept = getMapGraph().get(nextPosition);

        return nextPercept != null && (nextPercept.hasBlock() || !agentContainer.getAgentMap().getSelfPercept().equals(nextPercept)) && nextPercept.isBlocking(attachedPercept);
    }

    public boolean containsEdge(Direction edgeDirection) {
        int vision = agentContainer.getAgentPerceptContainer().getSharedPerceptContainer().getVision();
        if (vision == -1)
            return false;

        int edgeScalar = vision + 1;
        Position absolute = agentContainer.getCurrentLocation().add(edgeDirection.multiply(edgeScalar));
        return this.getMapGraph().containsKey(absolute);
    }


    /**
     * Creates the shortest path from the starting point to the destination.
     * @param startingPoint The starting position (usually the agent's current location)
     * @param destination The destination.
     * @return An array list of path positions to navigate to the destination, or null if a path could not be generated.
     * This occurs when there may be no path to the destination.
     */
    private synchronized List<Position> createADStarNavigation(Position startingPoint, Position destination) {

        Stopwatch stopwatch = Stopwatch.startTiming();

        // Create the search components (starting point, destination, etc.)
        SearchComponents<Double, Position, ?> components = GraphSearchProblem.startingFrom(startingPoint)
                .goalAt(destination)
                .in(agentContainer.getAgentMap().getMapGraph())
                .takeCostsFromEdges()
                .useHeuristicFunction(state -> Math.abs(destination.subtract(state).getDistance()))
                .components();
        ;

        ADStarForward adStarForward = Hipster.createADStar(components);
        Iterator<Node<Void, Position, ? extends ADStarNode<Void, Position, ?, ?>>> iterator = adStarForward.iterator();

        Node<Void, Position, ? extends ADStarNode<Void, Position, ?, ?>> node;
        do {
            node = iterator.next();
        } while (iterator.hasNext() && !node.state().equals(destination));

        long timedSearch = stopwatch.stopMS();
        System.out.println("Took " + timedSearch + " ms to search: " + node.pathSize());

        if(!node.state().equals(destination))
        {
            System.out.println("Failed to obtain a valid path to the destination: " + destination + ". Is the destination blocked?");
            return null;
        }

        // Convert AD Node to Position (aka states)
        List<Position> positions = node.path().stream().map(ADStarNode::state).collect(Collectors.toList());


//        List<Position> ends = getMapGraph().getConnected().get(getCurrentAgentPosition()).stream().map(GraphEdge::getVertex2).collect(Collectors.toList());
        Message.createAndSendPathMessage(agentContainer.getMqSender(), positions);
        return positions;

    }

}
