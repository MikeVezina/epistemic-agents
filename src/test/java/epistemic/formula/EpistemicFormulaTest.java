package epistemic.formula;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("EpistemicFormula Tests")
public class EpistemicFormulaTest {

    @ParameterizedTest
    @DisplayName(value = "Parsed Epistemic Formula")
    @ValueSource(strings = {"know(alice)", "~know(alice)", "know(~alice)"})
    public void testParseValidEpistemicFormula(String literal) throws ParseException {

        Literal parsedLiteral = ASSyntax.parseLiteral(literal);
        assertNotNull(parsedLiteral, "parsed object should not be null");

        EpistemicFormula parsedFormula = EpistemicFormula.parseLiteral(parsedLiteral);
        assertNotNull(parsedFormula, "parsed formula should not be null");
        assertEquals(parsedFormula.getOriginalLiteral(), parsedLiteral, "parsed formula should contain original literal");
    }


    @Test
    public void parseLiteralTest() {
    }

    @Test
    public void getRootLiteral() {
    }

    @Test
    public void getNextLiteral() {
    }

    @Test
    public void isEpistemicLiteral() {
    }

    @Test
    public void getOriginalLiteral() {
    }

    @Test
    public void getOriginalWrappedLiteral() {
    }

    @Test
    public void capply() {
    }
}