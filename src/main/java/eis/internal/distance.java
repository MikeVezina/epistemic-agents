package eis.internal;

import eis.iilang.Numeral;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import utils.Utils;

public class distance extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // execute the internal action

        ts.getAg().getLogger().fine("Executing internal action 'distance'");


        try {
            // Get the parameters
            Integer xArg = (int) (((NumberTermImpl) args[1]).solve());
            Integer yArg = (int) (((NumberTermImpl) args[2]).solve());

            double distance = Utils.Distance(xArg, yArg);

            Term distTerm = new NumberTermImpl(distance);

            // Unify
            boolean directionResult = un.unifies(distTerm, args[0]);

            // Return result
            return (directionResult);
        }
        // Deal with error cases
        catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'distance' received the wrong number of arguments.");
        } catch (ClassCastException e) {
            throw new JasonException(
                    "The internal action 'distance' received arguments that are of the wrong type.");
        } catch (Exception e) {
            throw new JasonException("Error in 'distance'.");
        }
    }
}
