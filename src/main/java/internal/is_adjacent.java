package internal;

import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import localization.LocalizationMapModel;
import localization.LocalizationMapView;

public class is_adjacent extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if(args.length < 3 || !(args[0] instanceof ObjectTerm))
            throw new JasonException("Missing arguments or argument not ObjectTerm");

        ObjectTerm viewObjectTerm = (ObjectTerm) args[0];
        LocalizationMapModel model = (LocalizationMapModel) viewObjectTerm.getObject();

        Location firstLoc = getLocationFromTerm(args[1]);
        Location secondLoc = getLocationFromTerm(args[2]);

        return model.isAdjacent(firstLoc, secondLoc);
    }

    private Location getLocationFromTerm(Term location)
    {
        try {
            Literal locLiteral = (Literal) location;
            int x = (int) ((NumberTerm) locLiteral.getTerm(0)).solve();
            int y = (int) ((NumberTerm) locLiteral.getTerm(1)).solve();
            return new Location(x, y);
        } catch (NoValueException e) {
            throw new RuntimeException("Failed to solve() location X/Y.", e);
        }
    }
}
