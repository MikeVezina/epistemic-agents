package eis.internal;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class select_requirement  extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {


        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action 'select_requirements'");

        List<AttachedDAO> attachedItems = new ArrayList<>();
        ts.getAg().getBB().getPercepts().forEachRemaining(percept -> {
            if (!percept.getFunctor().equalsIgnoreCase("attached"))
                return;

            AttachedDAO attachedPercept = new AttachedDAO(percept);
            attachedItems.add(attachedPercept);
        });

        Literal chosenOne = null;

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

