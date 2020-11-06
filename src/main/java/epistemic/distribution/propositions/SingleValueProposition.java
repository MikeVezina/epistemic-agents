package epistemic.distribution.propositions;

import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A PossiblyProposition is a mapping of a wrapped literal key and value in a given world.
 * The key corresponds to the literal that introduced the value (i.e. an expanded rule head).
 *
 * Both the key and value will be normalized literals since proposition literals can not be negated, contain custom namespaces, or contain annotations.
 */
public class SingleValueProposition extends Proposition {

    private final NormalizedWrappedLiteral singleValue;

    public SingleValueProposition(@NotNull NormalizedWrappedLiteral literalKey, @NotNull NormalizedWrappedLiteral literalValue) {
        super(literalKey);

        if (!literalValue.isGround())
            throw new IllegalArgumentException("literalValue is not ground");

        if(!literalValue.canUnify(getKey()))
            throw new IllegalArgumentException("The literalValue can not unify the literalKey. Failed to create Proposition.");

        this.singleValue = literalValue;
        super.getValue().add(singleValue);
    }

    public SingleValueProposition(@NotNull WrappedLiteral literalKey, @NotNull Literal literalValue) {
        this(literalKey.getNormalizedWrappedLiteral(), new WrappedLiteral(literalValue).getNormalizedWrappedLiteral());
    }

    public SingleValueProposition(@NotNull WrappedLiteral literalKey, @NotNull WrappedLiteral literalValue) {
        this(literalKey.getNormalizedWrappedLiteral(), literalValue.getNormalizedWrappedLiteral());
    }

    public WrappedLiteral getSingleValue()
    {
        return this.singleValue;
    }

    public Literal getValueLiteral() {
        return singleValue.getCleanedLiteral();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }

}


