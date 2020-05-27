package epistemic.wrappers;

import jason.asSyntax.Literal;
import org.junit.Assume;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.converters.NormWrappedLiteralArg;
import utils.converters.WrappedLiteralArg;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TestUtils.transformLiteralArguments;

class NormalizedWrappedLiteralTest {

    private static Stream<Arguments> validNegativesFixture() {
        return WrappedLiteralTest.validNegativesFixture();
    }

    private static Stream<Arguments> validFixtures() {
        return WrappedLiteralTest.validFixture();
    }

    private static Stream<Arguments> validPositivesFixture() {
        return WrappedLiteralTest.validPositivesFixture();
    }

    private static Stream<Arguments> negatedValueFixture() {
        return WrappedLiteralTest.negatedValueFixture();
    }


    @ParameterizedTest
    @MethodSource("negatedValueFixture")
    public void testCanUnify(@NormWrappedLiteralArg NormalizedWrappedLiteral key, @NormWrappedLiteralArg NormalizedWrappedLiteral value) {
        assertTrue(key.canUnify(value), "key and value should unify");
        assertTrue(value.canUnify(key), "unification should not be unidirectional");
    }

    @ParameterizedTest
    @MethodSource("validFixtures")
    public void testIsNormalized(@NormWrappedLiteralArg NormalizedWrappedLiteral normalized)
    {
        assertTrue(normalized.isNormalized(), "negated literal should be normalized");
        assertFalse(normalized.getCleanedLiteral().negated(), "the predicate indicator should remove the negation");
    }

    @ParameterizedTest
    @MethodSource("validPositivesFixture")
    public void testNegatedPredicateIndicator(@WrappedLiteralArg WrappedLiteral wrappedLiteral)
    {
        var normalized = wrappedLiteral.getNormalizedWrappedLiteral();
        assertEquals(wrappedLiteral.getCleanedLiteral().getPredicateIndicator(), normalized.getPredicateIndicator(), "the predicate indicator should be equal with no negation");
    }

    @ParameterizedTest
    @MethodSource("validNegativesFixture")
    public void testPositivePredicateIndicator(@WrappedLiteralArg WrappedLiteral wrappedLiteral)
    {
        var normalized = wrappedLiteral.getNormalizedWrappedLiteral();
        assertNotEquals(wrappedLiteral.getCleanedLiteral().getPredicateIndicator(), normalized.getPredicateIndicator(), "the predicate indicator should remove the negation");
    }
}