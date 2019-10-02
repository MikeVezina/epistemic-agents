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

    // Failure atoms
    private static final Atom FAIL_NO_PERCEPTION = new Atom("no_percept");
    private static final Atom FAIL_NO_PATH = new Atom("no_path");
    private static final Atom SUCCESS = new Atom("success");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {


        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
        Literal destination = (Literal) args[0];

        System.out.println("navigation_path called with: " + destination);

        if (!destination.getFunctor().equals("destination") && destination.getArity() != 2)
            throw new JasonException("Invalid Argument.");

        NumberTerm xTerm = (NumberTerm) destination.getTerm(0);
        NumberTerm yTerm = (NumberTerm) destination.getTerm(1);

        int x = (int) Utils.SolveNumberTerm(xTerm);
        int y = (int) Utils.SolveNumberTerm(yTerm);

        Position destinationPos = new Position(x, y);

        // Check if we have a perception of the destination
        if(!agentMap.getMapGraph().containsKey(destinationPos))
        {
            System.out.println("No Map perception for " + destinationPos + " exists.");
            return un.unifiesNoUndo(args[2], FAIL_NO_PERCEPTION);
        }


        ListTerm directionList = generatePath(agentMap, new Position(x, y));

        if (directionList == null)
            return un.unifiesNoUndo(args[2], FAIL_NO_PATH);

        // Unify
        return un.unifies(directionList, args[1]) && un.unifiesNoUndo(args[2], SUCCESS);
    }

    private ListTerm generatePath(AgentMap map, Position absolute) {
        List<Position> navPath = map.getAgentNavigation().getNavigationPath(absolute);

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