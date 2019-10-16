package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.percepts.Task;
import eis.watcher.SynchronizedPerceptWatcher;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import map.MapPercept;
import map.Position;
import utils.LiteralUtils;
import utils.PerceptUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class get_slave_meeting_point extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        SynchronizedPerceptWatcher perceptWatcher = SynchronizedPerceptWatcher.getInstance();
        AgentContainer agentContainer = perceptWatcher.getAgentContainer(ts.getUserAgArch().getAgName());

        String agentMaster = ((Atom) args[0]).getFunctor();
        AgentContainer masterContainer = perceptWatcher.getAgentContainer(agentMaster);
        Position master = agentContainer.getAgentAuthentication().getAuthenticatedTeammatePositions().get(masterContainer);

        if(master == null)
            return false;

        Structure reqStruct = ((Structure) args[1]);
        int reqX = LiteralUtils.GetNumberParameter(reqStruct, 0).intValue();
        int reqY = LiteralUtils.GetNumberParameter(reqStruct, 1).intValue();

        Position finalDestination = master.add(new Position(reqX, reqY));

        return un.unifies(args[2], createStructure(finalDestination));
    }

    private Structure createStructure(Position position) {
        Structure translatedStruct = new Structure("location");
        translatedStruct.addTerm(new NumberTermImpl(position.getX()));
        translatedStruct.addTerm(new NumberTermImpl(position.getY()));
        return translatedStruct;
    }

}
