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
import jason.runtime.RuntimeServices;
import utils.Utils;

public class rel_to_direction extends DefaultInternalAction {

	private static final long serialVersionUID = -6214881485708125130L;
	private static final String CLASS_NAME = rel_to_direction.class.getName();

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		
		// execute the internal action

		ts.getAg().getLogger().fine("Executing internal action '" + CLASS_NAME + "'");

		String callingCircumstance = "";

		if(ts.getC().getSelectedEvent() != null)
		{
			callingCircumstance = "event: " + ts.getC().getSelectedEvent().getTrigger().toString();
		} else if (ts.getC().getSelectedIntention() != null)
		{
			callingCircumstance = "intention: " + ts.getC().getSelectedIntention().peek().getTrigger().toString();
		}

		try {

			// Get the parameters
			Integer xArg = (int) Utils.SolveNumberTerm(args[1]);
			Integer yArg = (int) Utils.SolveNumberTerm(args[2]);


		//	ts.getLogger().info("rel_to_direction executed with current " + callingCircumstance + " with [" + xArg + ", " + yArg + "]");

			String dir = Utils.RelativeLocationToDirection(xArg, yArg);

			if(dir.isEmpty())
			{
				return false;
			}
			
			Atom direction = new Atom(dir);
			
			// Unify
			boolean directionResult = un.unifies(direction, args[0]);

			// Return result
			return (directionResult);
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