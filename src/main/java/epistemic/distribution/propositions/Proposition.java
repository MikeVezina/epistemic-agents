package epistemic.distribution.propositions;

import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSyntax.Literal;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Proposition extends AbstractMap.SimpleEntry<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> {
    protected Proposition(NormalizedWrappedLiteral key) {
        super(key, new HashSet<>());
    }

    @Override
    public NormalizedWrappedLiteral getKey() {
        return super.getKey();
    }

    public Literal getKeyLiteral() {
        return super.getKey().getCleanedLiteral();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }
}
