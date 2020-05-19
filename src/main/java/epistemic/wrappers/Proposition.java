package epistemic.wrappers;

import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Objects;

/**
 * A class that contains the mapping for a wrapped literal key and value in a given world.
 * The key corresponds to the literal that introduced the value.
 *
 * An example of this is a rule that gets expanded to introduce various values.
 * A proposition object would be created for each value. The key would be the rule head, and the
 * value would be one of the expanded values.
 */
public class Proposition extends AbstractMap.SimpleEntry<WrappedLiteral, WrappedLiteral> {


    public Proposition(@NotNull WrappedLiteral literalKey, @NotNull WrappedLiteral literalValue) {
        super(literalKey, literalValue);

        if (!literalValue.getLiteral().isGround())
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
        return super.getKey().getLiteral();
    }

    public Literal getValueLiteral() {
        return super.getValue().getLiteral();
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


