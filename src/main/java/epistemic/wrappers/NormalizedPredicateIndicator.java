package epistemic.wrappers;

import jason.asSyntax.PredicateIndicator;
import org.jetbrains.annotations.NotNull;

public class NormalizedPredicateIndicator {

    private final PredicateIndicator originalIndicator;
    private final PredicateIndicator normalizedIndicator;

    public NormalizedPredicateIndicator(@NotNull PredicateIndicator predicateIndicator) {
        this.originalIndicator = predicateIndicator;
        this.normalizedIndicator = createNormalizedIndicator(predicateIndicator);
    }

    public PredicateIndicator getOriginalIndicator() {
        return originalIndicator;
    }

    public PredicateIndicator getNormalizedIndicator() {
        return normalizedIndicator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NormalizedPredicateIndicator that = (NormalizedPredicateIndicator) o;
        return normalizedIndicator.equals(that.normalizedIndicator);
    }

    @Override
    public int hashCode() {
        return normalizedIndicator.hashCode();
    }

    public static PredicateIndicator createNormalizedIndicator(PredicateIndicator predicateIndicator) {
        var curFunctor = predicateIndicator.getFunctor();

        // Remove negation from functor
        if (curFunctor.startsWith("~"))
            curFunctor = curFunctor.substring(1);

        return new PredicateIndicator(predicateIndicator.getNS(), curFunctor, predicateIndicator.getArity());
    }
}
