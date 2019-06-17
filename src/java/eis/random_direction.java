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
import java.util.Random;

public class random_direction extends DefaultInternalAction {

	private static final long serialVersionUID = -6214881485708125130L;
	private java.util.Random random;
	
	public random_direction() {
		// TODO Auto-generated constructor stub
		random = new Random();
	}
	
	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		// execute the internal action

		ts.getAg().getLogger().fine("executing random_direction internal action");

		
		try {
			String direction = "n";
			
			switch (random.nextInt(4))
			{
				case 0:
					direction = "n";
					break;
				case 1:
					direction = "s";
					break;
				case 2:
					direction = "e";
					break;
				default:
					direction = "w";
					break;
			}
			
			
			// Create the result term
			Atom resultDirection = new Atom(direction);

			// Unify
			return un.unifies(resultDirection, args[0]);

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