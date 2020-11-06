package epistemic.distribution.propositions;

import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A PossiblyProposition is a mapping of a wrapped literal key and value in a given world.
 * The key corresponds to the literal that introduced the value (i.e. an expanded rule head).
 *
 * Both the key and value will be normalized literals since proposition literals can not be negated, contain custom namespaces, or contain annotations.
 */
public class MultiValueProposition extends Proposition {

    public MultiValueProposition(@NotNull NormalizedWrappedLiteral literalKey, @NotNull Set<NormalizedWrappedLiteral> literalValues) {
        super(literalKey);

        for(var value : literalValues) {
            if (!value.isGround())
                throw new IllegalArgumentException("literalValue is not ground");

            if (!value.canUnify(getKey()))
                throw new IllegalArgumentException("The literalValue can not unify the literalKey. Failed to create Proposition.");

            super.getValue().add(value);
        }
    }

    public MultiValueProposition(@NotNull WrappedLiteral literalKey, @NotNull Set<WrappedLiteral> literalValues) {
        this(literalKey.getNormalizedWrappedLiteral(), getLiteralVals(literalValues));
    }

    private static Set<NormalizedWrappedLiteral> getLiteralVals(Set<WrappedLiteral> literalValues) {
        return literalValues.stream().map(WrappedLiteral::getNormalizedWrappedLiteral).collect(Collectors.toSet());
    }

    @Override
    public Literal getKeyLiteral() {
        return null;
    }
}


