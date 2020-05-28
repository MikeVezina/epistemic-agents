package utils;

import epistemic.Proposition;
import epistemic.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import utils.converters.EpistemicFormulaConverter;
import utils.converters.LiteralConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class TestUtils {
    public static Map<WrappedLiteral, LinkedList<Literal>> createHandEnumeration(String agent, String... values) {
        var map = new HashMap<WrappedLiteral, LinkedList<Literal>>();
        var key = createHandWithVariable(agent);
        var valueList = new LinkedList<Literal>();

        for (String val : values) {
            valueList.add(createHandWithValue(agent, val).getOriginalLiteral());
        }

        map.put(key, valueList);
        return map;
    }

    public static Map<WrappedLiteral, Proposition> createHandEntry(String agent, String value) {

        var map = new HashMap<WrappedLiteral, Proposition>();

        var key = createHandWithVariable(agent);
        var val = createHandWithValue(agent, value);

        map.put(key, new Proposition(key,val));

        return map;
    }

    public static WrappedLiteral createHandWithValue(String termOne, String termTwo) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createString(termTwo)));
    }

    public static WrappedLiteral createHandWithVariable(String termOne, String varName) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar(varName)));
    }

    public static WrappedLiteral createHandWithVariable(String termOne) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar()));
    }

    /**
     * Utility to flatten variable length arguments into a stream of single arguments.
     * Allows us to re-use fixtures.
     * <br>
     * <br>
     * Example: A stream with the arguments:
     * <br>
     * { (1, 2), (3, 4), (5, 6) }
     * <br>
     * Will be flattened to:
     * <br>
     * { (1), (2), (3), (4), (5), (6) }
     * @param validFixture The variable length argument stream.
     * @return A Flattened arguments stream containing all arguments in the original stream.
     */
    public static Stream<Arguments> flattenArguments(@NotNull Stream<Arguments> validFixture) {
        return validFixture.flatMap((arguments) -> Arrays.stream(arguments.get()))
                .map(Arguments::of);
    }

    /**
     * Creates a WrappedLiteral object, suppressing any parse exception with a NullPointer (to reduce try/catch littering)
     * @param litString
     * @return
     */
    public static WrappedLiteral createWrappedLiteral(String litString) {
        try {
            return new WrappedLiteral(ASSyntax.parseLiteral(litString));
        } catch (ParseException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    public static <R> List<R> aggregateLists(List<R> subscribed, List<R> other) {
        List<R> list = new ArrayList<>();
        list.addAll(subscribed);
        list.addAll(other);
        return list;
    }

    public static Stream<Arguments> transformLiteralArguments(Stream<Arguments> argumentsStream, Function<List<Literal>, List<Literal>> function) {
        LiteralConverter converter = new LiteralConverter();

        return argumentsStream.map(arguments -> {
            List<Literal> literals = new ArrayList<>();

            for (var arg : arguments.get()) {
                Object converted = converter.convert(arg, null);

                if (converted == null) {
                    literals.add(null);
                    continue;
                }

                if (!(converted instanceof Literal))
                    throw new RuntimeException("Failed to convert " + arg + " to literal");

                literals.add((Literal) converted);
            }

            return Arguments.of(function.apply(literals).toArray());
        });
    }

    public static Literal createLiteral(Object source) {
        LiteralConverter converter = new LiteralConverter();
        return (Literal) converter.convert(source, null);
    }

    public static EpistemicFormula createFormula(Object source) {
        EpistemicFormulaConverter converter = new EpistemicFormulaConverter();
        return (EpistemicFormula) converter.convert(source, null);
    }
}
