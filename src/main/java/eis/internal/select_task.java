package eis.internal;

import cartago.util.agent.Percept;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class select_task extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action 'select_task'");

        Map<Literal, Integer> score = new HashMap<>();

        ts.getAg().getBB().getPercepts().forEachRemaining(percept -> {
            if (!percept.getFunctor().equalsIgnoreCase("task"))
                return;
            NumberTermImpl taskReward = (NumberTermImpl) percept.getTerm(2);
            int taskRewardVal = (int) taskReward.solve();
            score.put(percept, taskRewardVal);
        });

        Literal chosenOne = null;
        int highestScore = -1;
        for (Map.Entry<Literal, Integer> entry : score.entrySet()) {
            Integer reward = entry.getValue();
            if (reward > highestScore) {
                chosenOne = entry.getKey();
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
