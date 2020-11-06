package internal;

import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import localization.models.LocalizationMapModel;

import java.util.HashSet;
import java.util.Set;

public class merge_sets extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ListTerm listOfSets = (ListTerm) args[0];
        Set<Term> termSet = new HashSet<>();

        for (Term term : listOfSets.toArray(new Term[0])) {
            ListTerm internalList = (ListTerm) term;

            if (termSet.isEmpty()) {
                termSet.addAll(internalList.getAsList());
                continue;
            }

            termSet.retainAll(internalList.getAsList());
        }

        ListTerm finalResult = new ListTermImpl();
        finalResult.addAll(termSet);


        return un.unifies(args[1], finalResult);
    }

    private Location getLocationFromTerm(Term location) {
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
