package epistemic.wrappers;

import jason.asSyntax.Literal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import utils.converters.LiteralArg;
import utils.converters.WrappedLiteralArg;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TestUtils.*;

@DisplayName("WrappedLiteral Unit Tests")
public class WrappedLiteralTest {
    private static final String NO_TERM_KEY = "test";
    private static final String ONE_TERM_KEY = "test(asd)";
    private static final String ONE_TERM_VAR_KEY = "test(_)";
    private static final String TWO_TERM_ONE_VAR_TERM_KEY = "test(asd, Test)";
    private static final String TWO_TERM_TWO_VAR_TERM_KEY = "test(_, Test)";


    @ParameterizedTest
    @MethodSource("validFixture")
    public void testHashCode(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertEquals(key.hashCode(), value.hashCode(), "The hashes must be the same");
    }

    @ParameterizedTest
    @MethodSource("validFixture")
    public void testCanUnify(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertTrue(key.canUnify(value), "key and value should unify");
        assertTrue(value.canUnify(key), "unification should not be unidirectional");
    }

    @ParameterizedTest
    @MethodSource("negatedValueFixture")
    public void testInvalidCanUnify(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertFalse(key.canUnify(value), "key and value should not unify if negation does not match");
        assertFalse(value.canUnify(key), "unification should be unidirectional");
    }

    @ParameterizedTest(name = "Throw Exception on VarTerm Literal: {0}")
    @MethodSource("varWrappedLiterals")
    public void testVarTermWrappedLiteral(@LiteralArg Literal value) {
        var exception = assertThrows(IllegalArgumentException.class, () -> new WrappedLiteral(value));
        assertTrue(exception.getMessage().contains("Can not wrap a VarTerm literal: "), "provide exception message");
    }


    @ParameterizedTest
    @MethodSource("validFixture")
    public void testEquals(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertEquals(key, value, "The objects must be equal");
    }

    @ParameterizedTest
    @MethodSource("flatValidFixture")
    public void testNonWrappedEquals(@WrappedLiteralArg WrappedLiteral value) {
        Literal modifiedLiteral = value.getModifiedLiteral();
        assertEquals(value, modifiedLiteral, "The objects must be equal");
    }

    @ParameterizedTest
    @MethodSource("flatValidFixture")
    public void testNonWrappedNotEquals(@WrappedLiteralArg WrappedLiteral value) {
        String modifiedLiteral = value.getOriginalLiteral().toString();

        // Should not be equal since the passed-in object is a string
        assertNotEquals(value, modifiedLiteral, "The objects must be equal");
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
    @MethodSource("flatValidFixture")
    public void copy(@WrappedLiteralArg WrappedLiteral value) {
        var valCopy = value.copy();

        assertNotSame(value, valCopy, "key instances should not be the same");
        assertEquals(value, valCopy, "WrappedLiteral should be equal to a copy of itself");
        assertEquals(value.hashCode(), valCopy.hashCode(), "value hashcode should be equal to a copy hashcode");

    }

    /**
     * Tests the safe prop name in the valid fixture
     */
    @ParameterizedTest
    @MethodSource(value = {"flatValidFixture", "safePropNameFixture"})
    public void toSafePropNameValidFixture(@WrappedLiteralArg WrappedLiteral value) {
        assertAll("Safe Proposition Name Assertions",
                () -> assertFalse(value.toSafePropName().contains("("), "prop name should not contain open parenthesis"),
                () -> assertFalse(value.toSafePropName().contains(")"), "prop name should not contain close parenthesis"),
                () -> assertFalse(value.toSafePropName().contains(" "), "prop name should not contain spaces")
        );
    }


    @ParameterizedTest
    @MethodSource(value = "flatValidFixture")
    public void getOriginalLiteral(@LiteralArg Literal literal) {
        var literalCopy = literal.copy();
        var wrapped = new WrappedLiteral(literal);
        assertNotSame(literal, wrapped.getOriginalLiteral(), "the original literal should be cloned");
        assertEquals(literalCopy, wrapped.getOriginalLiteral(), "the original literal should not be modified in any way");
    }

    @ParameterizedTest
    @MethodSource(value = "flatValidFixture")
    public void getNormalizedWrappedLiteral(@LiteralArg Literal literal) {
        var wrappedNormalized = new WrappedLiteral(literal).getNormalizedWrappedLiteral();

        // We should never get back the same literal since it should be a clone
        assertNotSame(literal, wrappedNormalized.getOriginalLiteral());
        assertTrue(wrappedNormalized.isNormalized());
    }

    @ParameterizedTest
    @MethodSource(value = "flatValidFixture")
    public void getPredicateIndicator(@WrappedLiteralArg WrappedLiteral value) {
        assertEquals(value.getCleanedLiteral().getPredicateIndicator(), value.getPredicateIndicator(), "the predicate indicator should be obtained from the cleaned literal");
    }

    @ParameterizedTest
    @MethodSource(value = "validNegativesFixture")
    public void getNegatedPredicateIndicator(@WrappedLiteralArg WrappedLiteral value) {
        assertEquals(value.getCleanedLiteral().getPredicateIndicator(), value.getPredicateIndicator(), "the predicate indicator should maintain the negation");
    }

    @ParameterizedTest
    @MethodSource(value = "validNegativesFixture")
    public void isNormalized(@WrappedLiteralArg WrappedLiteral value) {
        assertFalse(value.isNormalized(), "negated literals should not be normalized");
    }

    @ParameterizedTest
    @ValueSource(strings = {"test(test('Hello World', nested('hello')))"})
    public void testNestedLiterals(@WrappedLiteralArg WrappedLiteral wrappedLiteral) {
        // Ensure that all terms are wrapped (including nested)
        assertWrappedTerms(wrappedLiteral.getModifiedLiteral());
    }


    /**
     * @return A flattened stream of valid fixtures for single-argument tests.
     */
    static Stream<Arguments> flatValidFixture() {
        return flattenArguments(validFixture());
    }

    static Stream<Arguments> validFixture() {
        return Stream.concat(validPositivesFixture(), validNegativesFixture());
    }

    static Stream<Arguments> validPositivesFixture() {

        return Stream.of(
                Arguments.of(NO_TERM_KEY, NO_TERM_KEY),

                // One term with var term
                Arguments.of(ONE_TERM_VAR_KEY, "test(_)"),
                Arguments.of(ONE_TERM_VAR_KEY, "test(OtherVar)"),

                // One term with ground term
                Arguments.of(ONE_TERM_KEY, "test(asd)"),

                // Matches the hash of the key with one variable term
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, _)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, Wow)"),
                Arguments.of(TWO_TERM_ONE_VAR_TERM_KEY, "test(asd, Test)"),

                // Matches the key with two variable terms
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(First, _)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, _)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, Second)"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(_, _)"),

                // The namespaces and annotations should be ignored.
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, Second)[source(self)]"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "ns::test(Test, Second)[source(self)]"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "NS::test(Test, Second)[source(self)]"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "test(Test, Second)[Annot]"),
                Arguments.of(TWO_TERM_TWO_VAR_TERM_KEY, "NS::test(Test, Second)[Annot]")
        );
    }


    @ParameterizedTest
    @MethodSource("unifyLitteredLiteralTerms")
    public void testCanUnifyLittered(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        assertTrue(key.canUnify(value), "key and value should unify");
        assertTrue(value.canUnify(key), "unification should not be unidirectional");
    }

    /**
     * @return An argument stream of inverted value literals
     */
    static Stream<Arguments> negatedValueFixture() {
        return transformLiteralArguments(validFixture(), (literals) -> {
            // Invert the value (at index 2)
            if (literals.size() < 2)
                return literals;

            // Invert the value
            var valueLiteral = literals.get(1);
            literals.set(1, valueLiteral.setNegated(valueLiteral.negated() ? Literal.LPos : Literal.LNeg));

            return literals;
        });
    }

    private static Stream<Arguments> unifyLitteredLiteralTerms() {
        return Stream.of(
                Arguments.of(
                        "kb::hand(ns::test, Ns::Val, wow, \"tester\")[no::annot, _]",
                        "hand(test, Val, wow, \"tester\")"
                )
        );
    }

    static Stream<Arguments> validNegativesFixture() {

        // Takes all literals from the positives fixture and negates them
        return transformLiteralArguments(validPositivesFixture(), (literals) ->
                literals.stream().map((l) -> l.setNegated(Literal.LNeg)).collect(Collectors.toList())
        );
    }


    static Stream<Arguments> safePropNameFixture() {
        return Stream.of(
                Arguments.of("hand('Spaces Spaces', 'Test')"),
                Arguments.of("hand('Spaces ( brackets )', 'Test')")
        );
    }

    static Stream<Arguments> invalidFixture() {

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

    private static Stream<Arguments> varWrappedLiterals() {
        return flattenArguments(
                Stream.of(
                        Arguments.of("_", "Test", "Var")
                )
        );
    }

}