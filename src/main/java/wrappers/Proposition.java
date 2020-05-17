package wrappers;

import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.AbstractMap;
import java.util.List;

/**
 * A class that contains the mapping for a wrapped literal key and value in a given world.
 * The key corresponds to the literal that introduced the value.
 *
 * An example of this is a rule that gets expanded to introduce various values.
 * A proposition object would be created for each value. The key would be the rule head, and the
 * value would be one of the expanded values.
 */
public class Proposition extends AbstractMap.SimpleEntry<WrappedLiteral, WrappedLiteral> {


    public Proposition(WrappedLiteral literalKey, WrappedLiteral literalValue) {
        super(literalKey, literalValue);
    }

    public Proposition(WrappedLiteral literalKey, Literal literalValue) {
        super(literalKey, new WrappedLiteral(literalValue));
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
}


