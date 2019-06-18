package eis;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

public class direction_to_rel extends DefaultInternalAction {

	private static final long serialVersionUID = -6214881485708125130L;

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		// execute the internal action

		ts.getAg().getLogger().fine("executing internal action 'UxVInternalActions.absolutePosition'");

		System.out.println("wow" + args[0].getClass().getName());
		
		
		try {
			// Get the parameters
			String direction = ((Atom) args[0]).getFunctor();

			int x = 0;
			int y = 0;
			
			if(direction.equalsIgnoreCase("w"))
				x = -1;
			if(direction.equalsIgnoreCase("e"))
				x = 1;
			if(direction.equalsIgnoreCase("n"))
				y = -1;
			if(direction.equalsIgnoreCase("s"))
				y = 1;
			
			
			// Create the result term
			NumberTerm resultX = new NumberTermImpl(x);
			NumberTerm resultY = new NumberTermImpl(y);

			// Unify
			boolean relPositionX = un.unifies(resultX, args[1]);
			boolean relPositionY = un.unifies(resultY, args[2]);

			// Return result
			return (relPositionX && relPositionY);
		}
		// Deal with error cases
		catch (ArrayIndexOutOfBoundsException e) {
			throw new JasonException("The internal action 'DirectionToRelativePosition' received the wrong number of arguements.");
		} catch (ClassCastException e) {
			throw new JasonException(
					"The internal action 'DirectionToRelativePosition' received arguements that are of the wrong type.");
		} catch (Exception e) {
			throw new JasonException("Error in 'DirectionToRelativePosition'.");
		}
	}
}