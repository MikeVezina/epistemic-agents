package eis.internal;

import eis.EISAdapter;
import map.MapPercept;
import eis.percepts.Task;
import eis.agent.AgentContainer;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import map.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class meeting_point extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;

    static ConcurrentMap<String, Map.Entry<AgentContainer, Position>> taskMeetingPoints;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (taskMeetingPoints == null)
            taskMeetingPoints = new ConcurrentHashMap<>();



        String taskName = ((Literal) args[0]).getFunctor();
        String agentName = ts.getUserAgArch().getAgName();

        AgentContainer agentContainer = EISAdapter.getSingleton().getAgentContainer(agentName);

        Map.Entry<AgentContainer, Position> meetingPoint = taskMeetingPoints.getOrDefault(taskName, null);

        if (meetingPoint != null)
            return un.unifies(createMeetingPointStructure(agentContainer, meetingPoint), args[1]);

        Task selectedTask = agentContainer.getAgentPerceptContainer().getSharedPerceptContainer().getTaskMap().getOrDefault(taskName, null);

        if(selectedTask == null)
        {
            System.out.println("Failed to provide task.");
            return false;
        }

        // Filter out any goal locations that are blocked, or if there are not enough free spaces to support all requirement blocks.
        List<MapPercept> goalPercepts = agentContainer.getAgentMap().getSortedGoalPercepts(p -> {
            if(agentContainer.getAgentMap().doesBlockAgent(p))
                return false;

            for(var r : selectedTask.getRequirementList())
            {
                Position relative = r.getPosition();
                MapPercept spacePercept = agentContainer.getAgentMap().getMapPercept(p.getLocation().add(relative));
                if(spacePercept == null || agentContainer.getAgentMap().doesBlockAgent(spacePercept))
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
        return un.unifies(createMeetingPointStructure(agentContainer, meetingPoint), args[1]);
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
