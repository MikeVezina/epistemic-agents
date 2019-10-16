package eis.internal;

import eis.EISAdapter;
import eis.agent.Rotation;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import map.AgentMap;

public class get_entity_name extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = get_rotations.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
        String agentName = ((Atom) args[0]).getFunctor();

//        AgentMap agentMap = EISAdapter.getSingleton().getEnvironmentInterface().get.getAgentMap(ts.getUserAgArch().getAgName());

        ListTerm rotationList = new ListTermImpl();


        return un.unifies(rotationList, args[0]);
    }
}
