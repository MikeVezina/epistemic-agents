package epistemic;

import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;

import java.util.HashSet;
import java.util.Set;

public class ManagedIndicators {
    private final Set<PredicateIndicator> managedIndicators;

    public ManagedIndicators() {
        this.managedIndicators = new HashSet<>();
    }

    public void addIndicator(Literal literal)
    {
        if(literal == null)
            return;

        addIndicator(literal.getPredicateIndicator());
    }

    public boolean isIndicator(Literal literal)
    {
        if(literal == null)
            return false;

        return isIndicator(literal.getPredicateIndicator());
    }

    public boolean isIndicator(PredicateIndicator indicator)
    {
        return this.managedIndicators.contains(indicator);
    }

    public void addIndicator(PredicateIndicator indicator)
    {
        this.managedIndicators.add(indicator);
    }
}
