package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.agent.Rotation;
import eis.map.AgentMap;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;

public class get_attached_blocks extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = get_attached_blocks.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        AgentContainer agentContainer = EISAdapter.getSingleton().getAgentContainer(ts.getUserAgArch().getAgName());

        // Go through the current agent's attached positions, and

        ListTerm rotationList = new ListTermImpl();

        for (Rotation r : agentContainer.getAgentMap().getAgentNavigation().getRotationDirections()) {
            rotationList.append(r.getAtom());
        }

        throw new RuntimeException("not implemented.");

//        return un.unifies(rotationList, args[0]);
    }
}