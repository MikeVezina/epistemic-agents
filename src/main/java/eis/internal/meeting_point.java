package eis.internal;

import eis.EISAdapter;
import eis.percepts.MapPercept;
import eis.percepts.Task;
import eis.agent.AgentContainer;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class meeting_point extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");

    static ConcurrentMap<String, Map.Entry<AgentContainer, Position>> taskMeetingPoints;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (taskMeetingPoints == null)
            taskMeetingPoints = new ConcurrentHashMap<>();


        String taskName = ((Literal) args[0]).getFunctor();
        String agent1Name = ((Literal) args[1]).getFunctor();

        AgentContainer agentContainer = EISAdapter.getSingleton().getAgentContainer(agent1Name);

        Map.Entry<AgentContainer, Position> meetingPoint = taskMeetingPoints.getOrDefault(taskName, null);

        if (meetingPoint != null)
            return un.unifies(createMeetingPointStructure(agentContainer, meetingPoint), args[2]);

        Task selectedTask = agentContainer.getAgentPerceptContainer().getSharedPerceptContainer().getTaskMap().getOrDefault(taskName, null);

        if(selectedTask == null)
        {
            System.out.println("Failed to provide task.");
            return false;
        }

        // Filter out any goal locations that are blocked
        List<MapPercept> goalPercepts = agentContainer.getAgentMap().getSortedGoalPercepts(p -> {
            if(agentContainer.getAgentMap().doesBlockAgent(p))
                return false;

            for(var r : selectedTask.getRequirementList())
            {
                Position relative = r.getPosition();
                MapPercept spacePercept = agentContainer.getAgentMap().getMapPercept(p.getLocation().add(relative));
                if(agentContainer.getAgentMap().doesBlockAgent(spacePercept))
                    return false;
            }
            return true;
        });

        if (goalPercepts.size() == 0)
            return false;

        MapPercept goalPercept = goalPercepts.get(0);
        Position p = goalPercept.getLocation();
        taskMeetingPoints.put(taskName, new HashMap.SimpleEntry<>(agentContainer, p));
        meetingPoint = taskMeetingPoints.getOrDefault(taskName, null);

        if (meetingPoint == null)
            return false;

        // Unify
        return un.unifies(createMeetingPointStructure(agentContainer, meetingPoint), args[2]);
    }

    private Structure createMeetingPointStructure(AgentContainer container, Map.Entry<AgentContainer, Position> meetingPoint) {
        AgentContainer originalContainer = meetingPoint.getKey();
        Position meetingPointPos = meetingPoint.getValue();

        if (!container.getAgentName().equals(originalContainer.getAgentName()))
            meetingPointPos = originalContainer.getAgentAuthentication().translateToAgent(container, meetingPointPos);

        return createStructure(meetingPointPos);

    }

    private Structure createStructure(Position position) {
        Structure translatedStruct = new Structure("location");
        translatedStruct.addTerm(new NumberTermImpl(position.getX()));
        translatedStruct.addTerm(new NumberTermImpl(position.getY()));
        return translatedStruct;
    }

}
