package utils.converters;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

/**
 * Converts individual test parameters into a WrappedLiteral object.
 * Accepts parameters of type: {null, String, Literal, WrappedLiteral}.
 * A null value will be returned as null.
 * A String will be parsed into a literal with {@link ASSyntax#parseLiteral(String)}.
 * A Literal will be wrapped into a WrappedLiteral and returned.
 */
public class WrappedLiteralConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (source == null)
            return null;

        if (source instanceof WrappedLiteral)
            return source;

        if (!(source instanceof String) && !(source instanceof Literal)) {
            throw new ArgumentConversionException("Source object (" + source + ") must be one of type [null, String, Literal, WrappedLiteral]");
        }

        Literal sourceLiteral;

        if (source instanceof String) {
            try {
                sourceLiteral = ASSyntax.parseLiteral((String) source);
            } catch (ParseException e) {
                throw new ArgumentConversionException("Failed to parse source literal: " + source + ". Message: " + e.getMessage());
            }
        } else {
            // source object is literal
            sourceLiteral = (Literal) source;
        }

        return new WrappedLiteral(sourceLiteral);
    }
}
