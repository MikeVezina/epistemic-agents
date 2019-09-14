package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentMap;
import eis.agent.Rotation;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import utils.Position;

public class calculate_rotation extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = get_rotations.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws NoValueException {
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());

        NumberTerm xOrigin = (NumberTerm) args[0];
        NumberTerm yOrigin = (NumberTerm) args[1];
        NumberTerm xDest = (NumberTerm) args[2];
        NumberTerm yDest = (NumberTerm) args[3];

        int xOriginInt = (int) xOrigin.solve();
        int yOriginInt = (int) yOrigin.solve();
        int xDestInt = (int) xDest.solve();
        int yDestInt = (int) yDest.solve();

        Position origin = new Position(xOriginInt, yOriginInt);
        Position destination = new Position(xDestInt, yDestInt);

        if(origin.equals(destination))
            return false;


        Position originRotated = Rotation.CLOCKWISE.rotate(origin);
        int numClockRotations = 1;

        while(!originRotated.equals(destination) && numClockRotations < 4)
        {
            originRotated = Rotation.CLOCKWISE.rotate(originRotated);
            numClockRotations++;
        }

        // We ended up back where we started, meaning there is no possible rotation since the destination was not equal to the origin
        if(numClockRotations == 4)
            return false;

        // We can achieve a shorter number of rotations with CCW
        if(numClockRotations > 2)
            return un.unifies(Rotation.COUNTER_CLOCKWISE.getAtom(), args[4]);

        return un.unifies(Rotation.CLOCKWISE.getAtom(), args[4]);
    }
}
