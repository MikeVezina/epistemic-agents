package jason;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.HashMap;
import java.util.Map;

public class is_valid_hand extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Map<Character, Integer> allCards = new HashMap<>();

        allCards.put('A', 0);
        allCards.put('8', 0);

        for(Term t : args)
        {
            if(!t.isString())
                throw new JasonException("The term (" + t.toString() + ") is not a string");

            String termString = ((StringTerm) t).getString();
            for(char c : termString.toCharArray())
            {
                if(!allCards.containsKey(c))
                    throw new JasonException("The term (" + t.toString() + ") is not part of a valid hand");

                allCards.computeIfPresent(c, (key, val) -> val + 1);
            }
        }

        int numAces = allCards.get('A');
        int numEights = allCards.get('8');
        int total = numAces + numEights;

        return numAces <= 4 && numEights <= 4 && total == 6;
    }
}
