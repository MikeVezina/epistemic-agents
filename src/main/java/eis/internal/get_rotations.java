package eis.internal;

import eis.EISAdapter;
import eis.percepts.agent.AgentMap;
import eis.percepts.agent.Rotation;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import utils.Direction;

public class get_rotations extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = get_rotations.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());

        ListTerm rotationList = new ListTermImpl();

        for (Rotation r : agentMap.getRotationDirections())
            rotationList.append(r.getAtom());

        return un.unifies(rotationList, args[0]);
    }
}
