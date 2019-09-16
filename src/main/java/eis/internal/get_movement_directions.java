package eis.internal;

import eis.EISAdapter;
import eis.map.AgentMap;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import eis.map.Direction;

public class get_movement_directions extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = get_rotations.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());

        ListTerm rotationList = new ListTermImpl();

        for(Direction dir : Direction.validDirections())
        {
            if(agentMap.canAgentMove(dir))
                rotationList.append(dir.getAtom());
        }

        return un.unifies(rotationList, args[0]);
    }
}
