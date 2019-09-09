package eis.internal;

import eis.EISAdapter;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.agent.AgentContainer;
import eis.percepts.agent.TaskList;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.PerceptUtils;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class select_task extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action 'select_task'");


        List<Literal> taskPercepts = new LinkedList<>();
        AgentContainer randomContainer = EISAdapter.getSingleton().getAgentContainers().values().stream().findAny().orElse(null);
        long curStep = randomContainer.getCurrentStep();

        List<Task> tasks = TaskList.getInstance().getTaskList().stream().filter(t -> t.getRequirementList().size() == 2 && t.getDeadline() > curStep).collect(Collectors.toList());

        if (tasks.isEmpty())
            return false;

        final Task chosenOne = tasks.stream().filter(t -> !t.getRequirementList().get(0).getBlockType().equals(t.getRequirementList().get(1).getBlockType())).findFirst().orElse(tasks.get(0));


        Percept taskPercept = TaskList.getInstance().getTaskListPercepts().stream().filter(t -> PerceptUtils.GetStringParameter(t, 0).equals(chosenOne.getName())).findFirst().orElse(null);

        try {
            // Unify
            Literal lit = EISAdapter.perceptToLiteral(taskPercept);
            boolean directionResult = un.unifiesNoUndo(new Structure(lit), args[0]);

            // Return result
            return (directionResult);
        }
        // Deal with error cases
        catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'select_task' received the wrong number of arguements.");
        } catch (ClassCastException e) {
            throw new JasonException(
                    "The internal action 'rel_to_direction' received arguements that are of the wrong type.");
        } catch (Exception e) {
            throw new JasonException("Error in 'rel_to_direction'.");
        }
    }

}
