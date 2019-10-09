package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.agent.AuthenticatedAgent;
import eis.percepts.requirements.Requirement;
import eis.watcher.SynchronizedPerceptWatcher;
import eis.percepts.Task;
import eis.percepts.containers.SharedPerceptContainer;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.*;
import java.util.stream.Collectors;

public class select_task extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;


    /**
     * select_task internal call should look like this:
     * select_task(FREE_AGENTS, RETURN)
     * <p>
     * Where:
     * FREE_AGENTS is a list of free agents that haven't been assigned a task yet.
     *
     * RETURN is a list of lists containing agent task assignments. It is possible for some agents to remain
     * free agents even after task assignment.
     *
     * RETURN example: [ [agent1, task4, req(0, 1, b1)], [agent3, task4, req(0, 2, b0)] ]
     *
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {



        /* Task Selection: (Where Op is a step completed by Operator AgentSpeak)
         * 1 (Op): Out of the list of agents that haven't been assigned a task, choose one agent (A).
         * 1.5: If agent has no authenticated team mates, tell them to keep exploring (no_teammates)
         * 2. For this one agent, see how many authenticated agents also have not been assigned a task
         * 3. Select a task with # requirements == # free agents
         * 4 (Op). Assign master to agent A, and slave to all authenticated free agents
         */


        // Get the free agent list
        // This is extremely messy but Im just trying to get it to work haha.
        List<String> freeAgents = getFreeAgentListFromTerm((ListTerm) args[0]);
        Map<Task, Map<String, Requirement>> agentAssignments = new HashMap<>();
        Set<String> assignedAgents = new HashSet<>();


        for (String agentName : freeAgents) {
            // Skip any agents that were processed from a previous teammate.
            if (assignedAgents.contains(agentName))
                continue;

            Map.Entry<Task, Map<String, Requirement>> taskMapEntry = getTeamAgentAssignment(agentName, freeAgents);

            if (taskMapEntry != null) {
                // Add the entry to the overall assignment map
                // Then add all agent names to the assigned set.
                agentAssignments.put(taskMapEntry.getKey(), taskMapEntry.getValue());
                assignedAgents.addAll(taskMapEntry.getValue().keySet());
            }
        }

        ListTerm resultListTerm = new ListTermImpl();

        for (var agentAssignment : agentAssignments.entrySet()) {
            Task teamTask = agentAssignment.getKey();
            Map<String, Requirement> agentRequirements = agentAssignment.getValue();

            for (var agentRequirement : agentRequirements.entrySet()) {
                ListTerm agentListTerm = new ListTermImpl();
                // Add Name, Task and Requirement Assignment to the list
                agentListTerm.add(new Atom(agentRequirement.getKey()));
                agentListTerm.add(new Atom(teamTask.getName()));
                agentListTerm.add(agentRequirement.getValue().toAtom());

                resultListTerm.add(agentListTerm);
            }

        }

        return un.unifies(args[1], resultListTerm);
    }

    private Map.Entry<Task, Map<String, Requirement>> getTeamAgentAssignment(String agentName, List<String> freeAgents) {

        // Get the agent's container
        AgentContainer agentContainer = SynchronizedPerceptWatcher.getInstance().getAgentContainer(agentName);
        List<AuthenticatedAgent> authenticatedAgents = agentContainer.getAgentAuthentication().getAuthenticatedAgents();

        if (authenticatedAgents.isEmpty())
            return null;


        // Filter out authenticated agents who have already been assigned a task
        List<String> freeTeamAgents = authenticatedAgents.stream().map(a -> a.getAgentContainer().getAgentName()).filter(freeAgents::contains).collect(Collectors.toList());

        // Sorts tasks by eligibility
        List<Task> collect = SynchronizedPerceptWatcher.getInstance().getSharedPerceptContainer().getTaskMap().getRemainingTasks().stream()

                // We should only accept tasks that have at most one requirement per team member (plus our self) and that are no bigger than our perception!
                .filter(t -> t.getRequirementList().size() <= Math.min(freeTeamAgents.size() + 1, agentContainer.getSharedPerceptContainer().getVision()))

                // Sort by deadline
                .sorted((t1, t2) -> {

                    // If the two tasks have the same number of requirements, sort by deadline (higher is better).
                    if (t1.getRequirementList().size() == t2.getRequirementList().size())
                        return t2.getDeadline() - t1.getDeadline();

                    return t1.getRequirementList().size() - t2.getRequirementList().size();
                }).collect(Collectors.toList());


        // If there are no tasks that match our filter, we just return an empty map
        if (collect.isEmpty())
            return null;

        Task selectedTask = collect.get(0);

        // Assign one teammate per requirement
        Map<String, Requirement> assignedTaskTeammates = new HashMap<>();

        // Get the list of task requirements.
        List<Requirement> requirementList = selectedTask.getRequirementList();

        // Assign the first requirement to our self
        assignedTaskTeammates.put(agentName, requirementList.get(0));

        for (int i = 1; i < requirementList.size(); i++) {

            String assignedTeammate = freeTeamAgents.get(i - 1);
            assignedTaskTeammates.put(assignedTeammate, requirementList.get(i));
        }

        return new HashMap.SimpleEntry<>(selectedTask, assignedTaskTeammates);
    }

    private List<String> getFreeAgentListFromTerm(ListTerm freeAgentList) {
        return freeAgentList.stream().map(a -> ((Atom) a).getFunctor()).collect(Collectors.toList());
    }

}
