package eis.internal;

import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class navigation_path extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        Literal destination = (Literal) args[0];

        if (!destination.getFunctor().equals("destination") && destination.getArity() != 2)
            throw new JasonException("Invalid Argument.");

        NumberTerm xTerm = (NumberTerm) destination.getTerm(0);
        NumberTerm yTerm = (NumberTerm) destination.getTerm(1);

        Atom direction = getNextDirection(xTerm, yTerm);

        // Unify
        return un.unifies(direction, args[1]);
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
        else
            return NORTH;

    }
}