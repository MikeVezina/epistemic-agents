package eis.internal;

import eis.EISAdapter;
import eis.listeners.SynchronizedPerceptWatcher;
import eis.map.Position;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.stdlib.atom;
import utils.LiteralUtils;
import utils.PerceptUtils;

import java.util.*;
import java.util.stream.Collectors;

public class authenticate_agents extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ListTerm agentList = (ListTerm) args[0];

        if(agentList.isEmpty())
            return un.unifies(agentList, args[1]);

        HashMap<Position, List<Structure>> agentPositionMap = new HashMap<>();

        for(Term t : agentList)
        {
            Structure agentStruct = (Structure) t;

            // Index 0 is the agent name.
            // Index 1 is the location.
            int x = LiteralUtils.GetNumberParameter(agentStruct, 2).intValue();
            int y = LiteralUtils.GetNumberParameter(agentStruct, 3).intValue();

            Position relPosition = new Position(x, y);

            List<Structure> agentAtomList = agentPositionMap.getOrDefault(relPosition, new ArrayList<>());
            agentAtomList.add(agentStruct);
            agentPositionMap.put(relPosition, agentAtomList);

        }

        // Remove any entries with multiple possibilities (aka value collisions)
        agentPositionMap.entrySet().removeIf(positionListEntry -> positionListEntry.getValue().size() > 1);

        // Remove any entries with no matching agents
        agentPositionMap.entrySet().removeIf(positionListEntry -> !agentPositionMap.containsKey(positionListEntry.getKey().negate()));

        ListTerm authAgentList = ASSyntax.createList();
        for(Map.Entry<Position, List<Structure>> entry : agentPositionMap.entrySet())
        {
            // Get agent structures
            Structure originAgent = entry.getValue().get(0);
            Structure perceivedAgent = agentPositionMap.get(entry.getKey().negate()).get(0);

            // Get agent names
            Atom originAgentName = (Atom) originAgent.getTerm(0);
            Atom perceivedAgentName = (Atom) perceivedAgent.getTerm(0);

            // Get agent location structures
            Structure originAgentLocation = (Structure) originAgent.getTerm(1);
            Structure perceivedAgentLocation = (Structure) perceivedAgent.getTerm(1);

            Position originPos = LiteralUtils.locationStructureToPosition(originAgentLocation);
            Position perceivedPos = LiteralUtils.locationStructureToPosition(perceivedAgentLocation);

//            // Get the relative positioning for the agents
//            NumberTerm xNumberTerm = ASSyntax.createNumber(entry.getKey().getX());
//            NumberTerm yNumberTerm = ASSyntax.createNumber(entry.getKey().getY());

            EISAdapter.getSingleton().authenticateAgent(originAgentName.getFunctor(), originPos, perceivedAgentName.getFunctor(), perceivedPos, entry.getKey());
            // Get the agent absolute locations
//            authAgentList.add(ASSyntax.createStructure("agent", originAgentName, originAgentLocation, perceivedAgentName, perceivedAgentLocation, xNumberTerm, yNumberTerm));
        }
        return true;
    }
}
