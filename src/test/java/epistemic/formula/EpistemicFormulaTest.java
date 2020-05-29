package epistemic.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import utils.converters.FormulaArg;
import utils.converters.LiteralArg;
import utils.converters.WrappedLiteralArg;

import java.util.stream.Stream;

import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("EpistemicFormula Tests")
public class EpistemicFormulaTest {

    @ParameterizedTest
    @DisplayName(value = "Parsed Epistemic Formula")
    @MethodSource(value = "parseValidFormulaFixture")
    public void testParseValidEpistemicFormula(@LiteralArg Literal formulaLiteral, @FormulaArg EpistemicFormula nextFormula, @WrappedLiteralArg WrappedLiteral rootLiteral) {
        EpistemicFormula formula = EpistemicFormula.fromLiteral(formulaLiteral);
        assertNotNull(formula, "formula literal should not be null");
        assertEquals(formula.getOriginalLiteral(), formulaLiteral, "parsed formula should contain original literal");
        assertEquals(formula.getOriginalWrappedLiteral().getOriginalLiteral(), formulaLiteral, "parsed formula should contain a wrapped original literal");
        assertEquals(nextFormula, formula.getNextFormula(), "parsed formula should have expected next formula");
        assertEquals(rootLiteral, formula.getRootLiteral(), "parsed formula should have expected root literal");
    }

    @ParameterizedTest
    @MethodSource(value = "parseValidFormulaFixture")
    public void testHashCode(@LiteralArg Literal formulaLiteral) {
        EpistemicFormula formula = EpistemicFormula.fromLiteral(formulaLiteral);
        assertNotNull(formula, "formula literal should not be null");

        WrappedLiteral wrappedOriginal = new WrappedLiteral(formulaLiteral);
        assertEquals(wrappedOriginal.hashCode(), formula.hashCode(), "hash code should return the wrapped original literal hashcode");
    }

    @ParameterizedTest
    @MethodSource(value = "parseInvalidFormulaFixture")
    public void testParseEpistemicFormulaNotLiteralTerm(@LiteralArg Literal formulaLiteral) {
        assertThrows(IllegalArgumentException.class, () -> EpistemicFormula.fromLiteral(formulaLiteral), "should throw an illegal argument exception for bad term");
    }

    private static Stream<Arguments> parseInvalidFormulaFixture() {
        return Stream.of(
                Arguments.of(
                        // Create a non-literal inner term
                        createLiteral("know", createNumber(1))
                )
        );
    }

    private static Stream<Arguments> parseValidFormulaFixture() {

        // Stream of Arguments with the format:
        // formulaLiteral, nextLiteral, rootLiteral
        return Stream.of(
                Arguments.of("know(alice)", createInnerFormula("alice"), "alice"),
                Arguments.of("~know(alice)", createInnerFormula("alice"), "alice"),
                Arguments.of("know(~alice)", createInnerFormula("~alice"), "~alice"),
                Arguments.of("~know(~alice)", createInnerFormula("~alice"), "~alice"),

                Arguments.of("possible(alice)", createInnerFormula("alice"), "alice"),
                Arguments.of("~possible(alice)", createInnerFormula("alice"), "alice"),
                Arguments.of("possible(~alice)", createInnerFormula("~alice"), "~alice"),
                Arguments.of("~possible(~alice)", createInnerFormula("~alice"), "~alice"),

                // meta-knowledge
                Arguments.of("know(know(alice))", "know(alice)", "alice"),
                Arguments.of("possible(~know(alice))", "~know(alice)", "alice"),
                Arguments.of("~know(know(~alice))", "know(~alice)", "~alice"),
                Arguments.of("~possible(~know(~alice))", "~know(~alice)", "~alice"),

                Arguments.of("know(possible(alice))", "possible(alice)", "alice"),
                Arguments.of("possible(~possible(alice))", "~possible(alice)", "alice"),
                Arguments.of("~know(possible(~alice))", "possible(~alice)", "~alice"),
                Arguments.of("~possible(~possible(~alice))", "~possible(~alice)", "~alice")
                );
    }

    /**
     * This helper method is hacky but necessary for accessing the package-private static method for creating a root epistemic formula. (Such as with the literal 'alice')
     * <p>
     * {@link EpistemicFormula#parseNextLiteralRecursive(Literal)}
     *
     * @return The inner epistemic formula
     */
    private static EpistemicFormula createInnerFormula(String literalString) {
        try {
            var literal = ASSyntax.parseLiteral(literalString);
            return EpistemicFormula.parseNextLiteralRecursive(literal);
        } catch (ParseException e) {
            throw new AssertionError(e.getMessage());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"know(alice)", "~know(alice)", "possible(alice)", "possible(possible(alice))", "~possible(alice)"})
    public void isEpistemicLiteral(@LiteralArg Literal literal) {
        assertTrue(EpistemicFormula.isEpistemicLiteral(literal), "should be epistemic literal");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"knows(alice)", "~know", "possible", "possible(hello, hello)", "~possible"})
    public void isInvalidEpistemicLiteral(@LiteralArg Literal literal) {
        assertFalse(EpistemicFormula.isEpistemicLiteral(literal), "should not be epistemic literal");
    }

    @ParameterizedTest
    @MethodSource(value = "applyUnifierFixture")
    public void capply(@FormulaArg EpistemicFormula original, Unifier unifier, @FormulaArg EpistemicFormula expected) {
        var resultFormula = original.capply(unifier);
        assertEquals(expected, resultFormula,"applied formula should be equal to expected value");
    }

    private static Stream<Arguments> applyUnifierFixture() {
        Unifier unifier = new Unifier();
        unifier.bind(ASSyntax.createVar("Card"), ASSyntax.createAtom("AA"));

        return Stream.of(
                Arguments.of("know(~know(alice(Card)))", unifier, "know(~know(alice('AA')))"),
                Arguments.of("know(~know(alice(Card, Var)))", unifier, "know(~know(alice('AA', Var)))"),
                Arguments.of("know(~know(alice(Card, _)))", unifier, "know(~know(alice('AA', _)))")
        );
    }
}