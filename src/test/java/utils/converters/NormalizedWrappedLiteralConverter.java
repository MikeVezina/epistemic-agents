package utils.converters;

import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

/**
 * Converts individual test parameters into a NormalizedWrappedLiteral object.
 * Accepts parameters of type: {null, String, Literal, WrappedLiteral, NormalizedWrappedLiteral}.
 * A null value will be returned as null.
 * A String will be parsed into a literal with {@link ASSyntax#parseLiteral(String)}.
 * A Literal will be wrapped into a NormalizedWrappedLiteral and returned.
 */
public class NormalizedWrappedLiteralConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {

        if (source == null)
            return null;

        if (source instanceof NormalizedWrappedLiteral)
            return source;

        if(source instanceof WrappedLiteral)
            return ((WrappedLiteral) source).getNormalizedWrappedLiteral();

        if (!(source instanceof String) && !(source instanceof Literal)) {
            throw new ArgumentConversionException("Source object (" + source + ") must be one of type [null, String, Literal, WrappedLiteral, NormalizedWrappedLiteral]");
        }

        // Delegate to LiteralConverter to obtain Literal from String/Literal object.
        LiteralConverter literalConverter = new LiteralConverter();
        Literal literal = (Literal) literalConverter.convert(source, context);

        return new NormalizedWrappedLiteral(literal);
    }
}
