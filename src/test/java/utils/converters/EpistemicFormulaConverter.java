package utils.converters;

import epistemic.distribution.formula.EpistemicFormula;
import jason.asSyntax.Literal;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

/**
 * Converts individual test parameters into a EpistemicFormulaConverter object.
 * Accepts parameters of type: {null, String, Literal}.
 */
public class EpistemicFormulaConverter implements ArgumentConverter {

    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if(source instanceof EpistemicFormula)
            return source;

        // Delegate to LiteralConverter to obtain Literal from String/Literal object.
        LiteralConverter literalConverter = new LiteralConverter();
        Literal literal = (Literal) literalConverter.convert(source, context);
        return EpistemicFormula.fromLiteral(literal);
    }
}
