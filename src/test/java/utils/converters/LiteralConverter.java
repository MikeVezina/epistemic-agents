package utils.converters;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

/**
 * Converts individual test parameters into a Literal object.
 * Accepts parameters of type: {null, String, Literal, WrappedLiteral}.
 * A null value will be returned as null.
 * A String will be parsed into a literal with {@link ASSyntax#parseLiteral(String)}.
 */
public class LiteralConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (source == null)
            return null;

        // Pass the object through if it is the correct type.
        if(context != null && context.getParameter().getType().isAssignableFrom(source.getClass()))
            return source;

        if (source instanceof Literal)
            return source;

        if (!(source instanceof String)) {
            throw new ArgumentConversionException("Source object (" + source + ") must be one of type {null, String, Literal, WrappedLiteral}");
        }

        try {
            return ASSyntax.parseLiteral((String) source);
        } catch (ParseException e) {
            throw new ArgumentConversionException("Failed to parse source literal: " + source + ". Message: " + e.getMessage());
        }
    }
}
