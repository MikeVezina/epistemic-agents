package eis.internal;

import eis.EISAdapter;
import eis.map.AgentMap;
import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import eis.map.Position;
import utils.Utils;

import java.util.List;

public class navigation_path extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
//        List<MapPercept> blockingPerceptions = agentMap.getRelativeBlockingPerceptions(5);


        Literal destination = (Literal) args[0];

        if (!destination.getFunctor().equals("destination") && destination.getArity() != 2)
            throw new JasonException("Invalid Argument.");

        NumberTerm xTerm = (NumberTerm) destination.getTerm(0);
        NumberTerm yTerm = (NumberTerm) destination.getTerm(1);

        int x = (int) Utils.SolveNumberTerm(xTerm);
        int y = (int) Utils.SolveNumberTerm(yTerm);

        ListTerm directionList = generatePath(agentMap, new Position(x, y));

        if(directionList == null)
            return false;

        // Unify
        return un.unifies(directionList, args[1]);
    }

    private ListTerm generatePath(AgentMap map, Position absolute) {
        List<Position> navPath = map.getNavigationPath(absolute);

        if(navPath == null)
        {
            Position relative = map.getAgentContainer().absoluteToRelativeLocation(absolute);
            Atom nextDir = getNextDirection(relative.getX(), relative.getY());
            return new ListTermImpl().append(nextDir);
        }
        return generatePathSequence(map.getCurrentAgentPosition(), navPath);
    }

    private ListTerm generatePathSequence(Position currentAgentPosition, List<Position> path)
    {
        ListTerm pathListTerm = new ListTermImpl();

        Position lastPos = currentAgentPosition;
        for(Position p : path)
        {
            Position dirPos = p.subtract(lastPos);

            if(dirPos.isZeroPosition())
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