package eis.internal;

import eis.EISAdapter;
import eis.percepts.AgentMap;
import eis.percepts.MapPercept;
import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.Direction;
import utils.Position;
import utils.Utils;

import java.util.List;

public class explore_direction extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
//        List<MapPercept> blockingPerceptions = agentMap.getRelativeBlockingPerceptions(5);

        // If there is an edge that isn't perceived, and we aren't blocked by an obstacle, move in that direction
        Atom dir = findEmptyEdge(agentMap);

        if(dir == null)
        {
            // Do something else
            System.out.println("No empty edge");
            return un.unifies(new Atom("s"), args[0]);
        }
        return un.unifies(dir, args[0]);
    }

    private Atom findEmptyEdge(AgentMap agentMap) {
        int edgeVision = AgentMap.GetVision() + 1;

        // Check west edge
        Position westEdge = agentMap.getCurrentAgentPosition().subtract(new Position(-edgeVision, 0));
        if(!agentMap.getMapKnowledge().containsKey(westEdge) && !agentMap.isAgentBlocked(Direction.WEST))
            return WEST;

        Position eastEdge = agentMap.getCurrentAgentPosition().subtract(new Position(edgeVision, 0));
        if(!agentMap.getMapKnowledge().containsKey(eastEdge) && !agentMap.isAgentBlocked(Direction.EAST))
            return EAST;

        Position southEdge = agentMap.getCurrentAgentPosition().subtract(new Position(0, edgeVision));
        if(!agentMap.getMapKnowledge().containsKey(southEdge) && !agentMap.isAgentBlocked(Direction.SOUTH))
            return SOUTH;

        Position northEdge = agentMap.getCurrentAgentPosition().subtract(new Position(0, -edgeVision));
        if(!agentMap.getMapKnowledge().containsKey(northEdge) && !agentMap.isAgentBlocked(Direction.NORTH))
            return NORTH;

        return null;
    }


    private ListTerm generatePath(AgentMap map, Position absolute) {
        List<Position> navPath = map.getNavigationPath(absolute);

        if (navPath == null) {
            Position relative = map.absoluteToRelativeLocation(absolute);
            Atom nextDir = getNextDirection(relative.getX(), relative.getY());
            return new ListTermImpl().append(nextDir);
        }
        return generatePathSequence(map.getCurrentAgentPosition(), navPath);
    }

    private ListTerm generatePathSequence(Position currentAgentPosition, List<Position> path) {
        ListTerm pathListTerm = new ListTermImpl();

        Position lastPos = currentAgentPosition;
        for (Position p : path) {
            Position dirPos = p.subtract(lastPos);

            if (dirPos.isZeroPosition())
                continue;

            pathListTerm.append(getNextDirection(dirPos));
            lastPos = p;
        }

        return pathListTerm;
    }

    private Atom getNextDirection(Position p) {
        return getNextDirection(p.getX(), p.getY());
    }

    private Atom getNextDirection(NumberTerm x, NumberTerm y) throws NoValueException {
        int xVal = (int) x.solve();
        int yVal = (int) y.solve();
        return getNextDirection(xVal, yVal);
    }

    private Atom getNextDirection(int x, int y) {
        if (x > 0)
            return EAST;
        else if (x < 0)
            return WEST;
        else if (y > 0)
            return SOUTH;
        else if (y < 0)
            return NORTH;
        else
            return null;

    }
}
