package eis.internal;

import eis.EISAdapter;
import eis.percepts.agent.AgentMap;
import eis.percepts.agent.Rotation;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import utils.Direction;
import utils.Utils;

public class is_blocked extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = is_blocked.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());

        Literal dirLiteral = (Literal) args[0];
        String dirStr = dirLiteral.getFunctor();
        Direction dir = Utils.DirectionToRelativeLocation(dirStr);

        if(dir == null)
            throw new JasonException("Failed to get direction.");

        return agentMap.isAgentBlocked(dir);
    }
}
