package eis.internal;

import jason.JasonException;
import jason.asSyntax.Term;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTermImpl;
import utils.Utils;

public class is_beside_agent extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = is_beside_agent.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action '" + CLASS_NAME + "'");

        String callingCircumstance = "";

        if (ts.getC().getSelectedEvent() != null) {
            callingCircumstance = "event: " + ts.getC().getSelectedEvent().getTrigger().toString();
        } else if (ts.getC().getSelectedIntention() != null) {
            callingCircumstance = "intention: " + ts.getC().getSelectedIntention().peek().getTrigger().toString();
        }

        try {
            ts.getLogger().info(CLASS_NAME + " executed with current " + callingCircumstance);

            // Get the parameters
            Integer xArg = (int) Utils.SolveNumberTerm(args[0]);
            Integer yArg = (int) Utils.SolveNumberTerm(args[1]);


            String dir = Utils.RelativeLocationToDirection(xArg, yArg);

            if(dir.isEmpty())
            {
                ts.getLogger().info(CLASS_NAME + " internal action called: Not beside agent.");
                return false;
            }

            return true;
        }
        // Deal with error cases
        catch (ArrayIndexOutOfBoundsException e) {
            Utils.DumpIntentionStack(ts);
            throw new JasonException("The internal action 'rel_to_direction' received the wrong number of arguements.");
        } catch (ClassCastException e) {
            Utils.DumpIntentionStack(ts);
            throw new JasonException(
                    "The internal action 'rel_to_direction' received arguments that are of the wrong type.");
        } catch (Exception e) {
            Utils.DumpIntentionStack(ts);
            throw new JasonException("Error in 'rel_to_direction': " + e.getLocalizedMessage());
        }
    }
}