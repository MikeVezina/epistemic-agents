package utils;

import epistemic.Proposition;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
}
