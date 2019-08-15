package eis.internal;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import utils.Utils;

public class rel_to_direction extends DefaultInternalAction {

	private static final long serialVersionUID = -6214881485708125130L;

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		// execute the internal action

		ts.getAg().getLogger().fine("Executing internal action 'rel_to_direction'");

		String callingCircumstance = "";

		if(ts.getC().getSelectedEvent() != null)
		{
			callingCircumstance = "event: " + ts.getC().getSelectedEvent().getTrigger().toString();
		} else if (ts.getC().getSelectedIntention() != null)
		{
			callingCircumstance = "intention: " + ts.getC().getSelectedIntention().peek().getTrigger().toString();
		}

		try {
			ts.getLogger().info("rel_to_direction executed with current " + callingCircumstance);

			// Get the parameters
			Integer xArg = (int)(((NumberTermImpl) args[1]).solve());
			Integer yArg = (int)(((NumberTermImpl) args[2]).solve());
			
			if(Math.abs(xArg) > 1) {
				ts.getLogger().severe("The internal action 'rel_to_direction' has received invalid input for X: " + xArg);
				return false;
			}
			
			if(Math.abs(yArg) > 1) {
				ts.getLogger().severe("The internal action 'rel_to_direction' has received invalid input for Y: " + yArg);
				return false;
			}
			
			if(Math.abs(xArg) + Math.abs(yArg) != 1) {
				ts.getLogger().severe("The internal action 'rel_to_direction' has received invalid input. The relative direction must be for {N,S,E,W}. X = " + xArg + ", Y = " + yArg);
				return false;
			}

		
			
			String dir = Utils.RelativeLocationToDirection(xArg,  yArg);
			
			Atom direction = new Atom(dir);
			
			// Unify
			boolean directionResult = un.unifies(direction, args[0]);

			// Return result
			return (directionResult);
		}
		// Deal with error cases
		catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'rel_to_direction' received the wrong number of arguements.");
		} catch (ClassCastException e) {
			throw new JasonException(
					"The internal action 'rel_to_direction' received arguments that are of the wrong type.");
		} catch (Exception e) {
			throw new JasonException("Error in 'rel_to_direction'.");
		}
	}
}