package eis.internal;

import eis.EISAdapter;
import eis.map.AgentMap;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import eis.map.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class explore_direction extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");
    private ConcurrentMap<String, Direction> lastDirectionMap;


    public explore_direction()
    {
        lastDirectionMap = new ConcurrentHashMap<>();
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());

        // If there is an edge that isn't perceived, and we aren't blocked by an obstacle, move in that direction
        Direction nextDir = findEmptyEdge(agentMap);

        if (nextDir != null) {
            lastDirectionMap.put(agentMap.getAgentName(), nextDir);
            return un.unifies(nextDir.getAtom(), args[0]);
        }

        Direction lastDirection = lastDirectionMap.get(agentMap.getAgentContainer().getAgentName());

        // Do something else
        System.out.println("No empty edge. Using random direction.");

        // If there hasn't been a generated direction, or if the direction is blocked,
        // generate a new direction.
        if(lastDirection == null || !agentMap.canAgentMove(lastDirection)) {
            Direction dir = getRandomUnblockedDirection(agentMap, lastDirection);
            lastDirectionMap.put(agentMap.getAgentName(), dir);
            return un.unifies(dir.getAtom(), args[0]);
        }

        return un.unifies(lastDirection.getAtom(), args[0]);
    }

    private Direction getRandomUnblockedDirection(AgentMap agentMap, Direction lastDir) {
        List<Direction> unblockedDirections = new ArrayList<>();
        for (Direction dir : Direction.validDirections()) {
            if (agentMap.canAgentMove(dir))
                unblockedDirections.add(dir);
        }
        if(unblockedDirections.isEmpty())
            return Direction.WEST;

        // Remove the opposite of the last direction (so that we don't bounce back in the direction we just came from)
        // only do this if we don't have other options
        if(unblockedDirections.size() > 1 && lastDir != null)
        {
          //  unblockedDirections.removeIf(d -> d.getPosition().add(lastDir.getPosition()).equals(Position.ZERO));
        }

        Random r = new Random();
        int index = r.nextInt(unblockedDirections.size());
        return unblockedDirections.get(index);
    }

    private Direction findEmptyEdge(AgentMap agentMap) {
        Direction lastDirection = lastDirectionMap.get(agentMap.getAgentContainer().getAgentName());


        if(lastDirection != null)
        {
            if (!agentMap.containsEdge(lastDirection) && agentMap.canAgentMove(lastDirection))
                return lastDirection;
        }

        if (!agentMap.containsEdge(Direction.WEST) && agentMap.canAgentMove(Direction.WEST))
            return Direction.WEST;

        if (!agentMap.containsEdge(Direction.NORTH) && agentMap.canAgentMove(Direction.NORTH))
            return Direction.NORTH;

        if (!agentMap.containsEdge(Direction.EAST) && agentMap.canAgentMove(Direction.EAST))
            return Direction.EAST;

        if (!agentMap.containsEdge(Direction.SOUTH) && agentMap.canAgentMove(Direction.SOUTH))
            return Direction.SOUTH;



        return null;
    }

}
