package eis.internal;

import eis.EISAdapter;
import eis.percepts.Task;
import eis.percepts.agent.AgentContainer;
import eis.percepts.agent.AgentMap;
import eis.percepts.agent.TaskList;
import eis.percepts.terrain.Goal;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class meeting_point extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        String taskName = ((Literal) args[0]).getFunctor();
        String agent1Name = ((Literal) args[1]).getFunctor();
        String agent2Name = ((Literal) args[2]).getFunctor();

        AgentContainer agent1Container = EISAdapter.getSingleton().getAgentContainer(agent1Name);
        AgentContainer agent2Container = EISAdapter.getSingleton().getAgentContainer(agent2Name);

        Task selectedTask = TaskList.getInstance().getTaskMap().getOrDefault(taskName, null);

//        agent1Container.getAgentMap().getSortedGoalPercepts(p -> {
//           agent1Container.getAgentMap().doesBlockAgent()
//        });

        // Unify
        return un.unifies(null, args[1]);
    }

}
