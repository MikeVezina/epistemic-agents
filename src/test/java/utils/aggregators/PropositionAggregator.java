package utils.aggregators;

import utils.converters.WrappedLiteralConverter;
import epistemic.Proposition;
import epistemic.wrappers.WrappedLiteral;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregates two string/literal/wrappedliteral parameters into a proposition object.
 * Utilizes the {@link WrappedLiteralConverter} to convert the parameters into a WrappedLiteral object.
 */
public class PropositionAggregator implements ArgumentsAggregator {
    /**
     * @param accessor The accessor object containing the test parameters
     * @param context The test context
     * @return A Proposition Object.
     * @throws ArgumentsAggregationException Thrown when the accessor is null,
     * does not have exactly two arguments, when the key or value argument is null,
     * or if the converted object is null.
     */
    @Override
    public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws ArgumentsAggregationException {
        if(accessor == null || accessor.size() != 2)
            throw new ArgumentsAggregationException("There must be two provided test objects");

        // Convert objects to Wrapped Literals using the converter
        var wrappedConverter = new WrappedLiteralConverter();

        var keyObj = accessor.get(0);
        var valObj = accessor.get(1);

        if(keyObj == null || valObj == null)
            throw new ArgumentsAggregationException("The key or value is null. Proposition does not accept null WrappedLiterals.");

        var wrappedKeyObj = wrappedConverter.convert(keyObj, context);
        var wrappedValObj = wrappedConverter.convert(valObj, context);

        if(wrappedKeyObj == null || wrappedValObj == null)
            throw new ArgumentsAggregationException("Failed to convert values to WrappedLiteral: " + accessor.toString());

        // Cast converted objects to WrappedLiterals
        WrappedLiteral wrappedKey = (WrappedLiteral) wrappedKeyObj;
        WrappedLiteral wrappedVal = (WrappedLiteral) wrappedValObj;

        return new Proposition(wrappedKey, wrappedVal);
    }
}
