package epistemic;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Objects;

/**
 * A Proposition is a mapping of a wrapped literal key and value in a given world.
 * The key corresponds to the literal that introduced the value (i.e. an expanded rule head).
 *
 * Both the key and value will be normalized literals since proposition literals can not be negated, contain custom namespaces, or contain annotations.
 */
public class Proposition extends AbstractMap.SimpleEntry<WrappedLiteral, WrappedLiteral> {

    public Proposition(@NotNull WrappedLiteral literalKey, @NotNull WrappedLiteral literalValue) {
        super(literalKey.getNormalizedWrappedLiteral(), literalValue.getNormalizedWrappedLiteral());

        if (!this.getValue().getOriginalLiteral().isGround())
            throw new IllegalArgumentException("literalValue is not ground");

        if(!literalValue.canUnify(literalKey))
            throw new IllegalArgumentException("The literalValue can not unify the literalKey. Failed to create Proposition.");
    }

    public Proposition(@NotNull WrappedLiteral literalKey, @NotNull Literal literalValue) {
        this(literalKey, new WrappedLiteral(literalValue));
    }

    @Override
    public WrappedLiteral getKey() {
        return super.getKey();
    }

    @Override
    public WrappedLiteral getValue() {
        return super.getValue();
    }

    public Literal getKeyLiteral() {
        return super.getKey().getOriginalLiteral();
    }

    public Literal getValueLiteral() {
        return super.getValue().getOriginalLiteral();
    }

    @Override
    public WrappedLiteral setValue(WrappedLiteral value) {
        return super.setValue(value);
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


