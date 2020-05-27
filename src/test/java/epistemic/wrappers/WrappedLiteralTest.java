package epistemic.wrappers;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.converters.WrappedLiteralArg;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WrappedLiteral Unit Tests")
public class WrappedLiteralTest {
    private static final WrappedLiteral NO_TERM_KEY = createWrappedLiteral("test");
    private static final WrappedLiteral ONE_TERM_KEY = createWrappedLiteral("test(asd)");
    private static final WrappedLiteral ONE_TERM_VAR_KEY = createWrappedLiteral("test(_)");
    private static final WrappedLiteral TWO_TERM_ONE_VAR_TERM_KEY = createWrappedLiteral("test(asd, Test)");
    private static final WrappedLiteral TWO_TERM_TWO_VAR_TERM_KEY = createWrappedLiteral("test(_, Test)");

    @ParameterizedTest
    @MethodSource("validFixture")
    public void testHashCode(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertEquals(key.hashCode(), value.hashCode(), "The hashes must be the same");
    }

    @ParameterizedTest
    @MethodSource("validFixture")
    public void testCanUnify(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertTrue(key.canUnify(value), "key and value should unify");
    }

    @ParameterizedTest
    @MethodSource("validFixture")
    public void testEquals(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertEquals(key, value, "The objects must be equal");
    }

    @ParameterizedTest
    @MethodSource("invalidFixture")
    public void testInvalidHashCode(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertNotEquals(key.hashCode(), value.hashCode(), "The hashes must not be the same");
    }

    @ParameterizedTest
    @MethodSource("invalidFixture")
    public void testNotEquals(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertNotEquals(key, value, "The objects must not be equal");
    }

    @ParameterizedTest
    @MethodSource("validFixture")
    public void copy(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        // Use each key,val from valid fixtures to test copy
        var keyCopy = key.copy();
        var valCopy = value.copy();

        assertEquals(key, keyCopy, "key should be equal to a copy of itself");
        assertEquals(value, valCopy, "value should be equal to a copy of itself");

        assertEquals(key.hashCode(), keyCopy.hashCode(), "key hashcode should be equal to copy hashcode");
        assertEquals(value.hashCode(), valCopy.hashCode(), "value hashcode should be equal to a copy hashcode");

        assertNotSame(key, keyCopy, "key instances should not be the same");
        assertNotSame(value, valCopy, "key instances should not be the same");
    }


    public static Stream<Arguments> validFixture() {

        return Stream.of(
                Arguments.of(NO_TERM_KEY, NO_TERM_KEY.copy()),
                Arguments.of(NO_TERM_KEY, "test"),

                // One term with var term
                Arguments.of(ONE_TERM_VAR_KEY, ONE_TERM_VAR_KEY.copy()),
                Arguments.of(ONE_TERM_VAR_KEY, "test(_)"),
                Arguments.of(ONE_TERM_VAR_KEY, "test(OtherVar)"),

                // One term with ground term
                Arguments.of(ONE_TERM_KEY, ONE_TERM_KEY.copy()),
                Arguments.of(ONE_TERM_KEY, "test(asd)"),

                // Matches the hash of the key with one variable term
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, TWO_TERM_ONE_VAR_TERM_KEY.copy()),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, _)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, Wow)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, Test)"),

                // Matches the key with two variable terms
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, TWO_TERM_TWO_VAR_TERM_KEY.copy()),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(First, _)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, _)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, Second)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(_, _)")


        );
    }

    public static Stream<Arguments> invalidFixture() {

        // These are the invalid fixtures that should not be equal.
        return Stream.of(

                // Literals with no terms (aka atoms)
                Arguments.of(NO_TERM_KEY, "test2"),

                // One term with var term
                Arguments.of(ONE_TERM_VAR_KEY, NO_TERM_KEY),
                Arguments.of(ONE_TERM_VAR_KEY, ONE_TERM_KEY),

                // One term with ground term
                Arguments.of(ONE_TERM_KEY, NO_TERM_KEY),
                Arguments.of(ONE_TERM_KEY, "test(wrong)"),
                Arguments.of(ONE_TERM_KEY, "test(_)"),

                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, NO_TERM_KEY),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, ONE_TERM_KEY),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, ONE_TERM_VAR_KEY),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, asd)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(Test, asd)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(_, asd)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(_, _)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, asd)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "testo(asd, Test)"),

                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, NO_TERM_KEY),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, ONE_TERM_KEY),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, ONE_TERM_VAR_KEY),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, TWO_TERM_ONE_VAR_TERM_KEY),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(asd, asd)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(_)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(_, asd)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(asd, _)")

        );

    }

    @Test
    public void toSafePropName() {
    }


    @Test
    public void getOriginalLiteral() {
    }

    @Test
    public void getNormalizedLiteral() {
    }

    @Test
    public void getNormalizedWrappedLiteral() {
    }

    @Test
    public void getPredicateIndicator() {
    }

    private static WrappedLiteral createWrappedLiteral(String litString) {
        try {
            return new WrappedLiteral(ASSyntax.parseLiteral(litString));
        } catch (ParseException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }
}