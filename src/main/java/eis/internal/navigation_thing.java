package eis.internal;

import eis.EISAdapter;
import eis.percepts.agent.AgentMap;
import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.Position;
import utils.Utils;

import java.util.List;

public class navigation_thing extends DefaultInternalAction {


    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());


        Literal type = (Literal) args[0];
        Literal details = (Literal) args[1];

        ListTerm directionList = generatePath(agentMap, type.getFunctor(), details.getFunctor());


        if (directionList == null)
            return false;

        // Unify
        return un.unifies(directionList, args[2]);
    }

    private ListTerm generatePath(AgentMap map, String type, String details) {
        List<Position> navPath = map.getNavigationPath(type, details);

        // Null path means we need to explore (there is nothing that could be found)
        if (navPath == null)
            return null;

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
