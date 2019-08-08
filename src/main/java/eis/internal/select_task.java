package eis.internal;

import cartago.util.agent.Percept;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.Utils;

import java.util.*;

public class select_task extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private Integer currentStep = 0;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action 'select_task'");


        List<Literal> taskPercepts = new LinkedList<>();

        ts.getAg().getBB().getPercepts().forEachRemaining(percept -> {
            if (percept.getFunctor().equalsIgnoreCase("task"))
            {
                taskPercepts.add(percept);
            }

            if (percept.getFunctor().equalsIgnoreCase("step"))
            {
                this.currentStep = (int) Utils.SolveNumberTerm(percept.getTerm(0));
            }
        });

        Literal chosenOne = null;
        int highestScore = -1;

        for (Literal task : taskPercepts) {
            int reward = (int) Utils.SolveNumberTerm(task.getTerm(2));
            int stepDeadline = (int) Utils.SolveNumberTerm(task.getTerm(1));

            if (stepDeadline >= currentStep && reward > highestScore) {
                chosenOne = task;
                highestScore = reward;
            }
        }

        if(chosenOne == null)
            return false;

        try {
            // Unify
            boolean directionResult = un.unifiesNoUndo(new Structure(chosenOne), args[0]);

            // Return result
            return (directionResult);
        }
        // Deal with error cases
        catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'rel_to_direction' received the wrong number of arguements.");
        } catch (ClassCastException e) {
            throw new JasonException(
                    "The internal action 'rel_to_direction' received arguements that are of the wrong type.");
        } catch (Exception e) {
            throw new JasonException("Error in 'rel_to_direction'.");
        }
    }
}
