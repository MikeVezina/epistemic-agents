package epistemic.wrappers;

import jason.asSyntax.Literal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.TestUtils;
import utils.converters.LiteralArg;
import utils.converters.NormWrappedLiteralArg;
import utils.converters.WrappedLiteralArg;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testFullLiteralCleaned(@NormWrappedLiteralArg NormalizedWrappedLiteral key, @NormWrappedLiteralArg NormalizedWrappedLiteral value) {
        assertTrue(key.isNormalized(), "key should be normalized");
        assertTrue(value.isNormalized(), "value should be normalized");

        TestUtils.assertNormalizedTerms(key.getCleanedLiteral());
        TestUtils.assertNormalizedTerms(value.getCleanedLiteral());

    }


    @ParameterizedTest
    @MethodSource("negatedValueFixture")
    public void testKeepOriginal(@LiteralArg Literal key, @LiteralArg Literal val) {

        var wrappedKey = new WrappedLiteral(key);
        var normWrappedKey = wrappedKey.getNormalizedWrappedLiteral();

        var wrappedVal = new WrappedLiteral(val);
        var normWrappedVal = wrappedVal.getNormalizedWrappedLiteral();


        // The original values should be maintained after creating the normalized wrapped
        assertEquals(key, normWrappedKey.getOriginalLiteral());
        assertEquals(val, normWrappedVal.getOriginalLiteral());
    }

    @ParameterizedTest
    @MethodSource("negatedValueFixture")
    public void testCanUnify(@NormWrappedLiteralArg NormalizedWrappedLiteral key, @NormWrappedLiteralArg NormalizedWrappedLiteral value) {
        assertTrue(key.canUnify(value), "key and value should unify");
        assertTrue(value.canUnify(key), "unification should not be unidirectional");
    }

    @ParameterizedTest
    @MethodSource("validFixtures")
    public void testIsNormalized(@NormWrappedLiteralArg NormalizedWrappedLiteral normalized) {
        assertTrue(normalized.isNormalized(), "negated literal should be normalized");
        assertFalse(normalized.getCleanedLiteral().negated(), "the predicate indicator should remove the negation");
    }

    @ParameterizedTest
    @MethodSource("validPositivesFixture")
    public void testNegatedPredicateIndicator(@WrappedLiteralArg WrappedLiteral wrappedLiteral) {
        var normalized = wrappedLiteral.getNormalizedWrappedLiteral();
        assertEquals(wrappedLiteral.getCleanedLiteral().getPredicateIndicator(), normalized.getPredicateIndicator(), "the predicate indicator should be equal with no negation");
    }

    @ParameterizedTest
    @MethodSource("validNegativesFixture")
    public void testPositivePredicateIndicator(@WrappedLiteralArg WrappedLiteral wrappedLiteral) {
        var normalized = wrappedLiteral.getNormalizedWrappedLiteral();
        assertNotEquals(wrappedLiteral.getCleanedLiteral().getPredicateIndicator(), normalized.getPredicateIndicator(), "the predicate indicator should remove the negation");
    }
}