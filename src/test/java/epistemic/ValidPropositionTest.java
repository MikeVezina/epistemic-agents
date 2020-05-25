package epistemic;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ValidPropositionTest {

    private static final String TEST_FUNCTOR = "hand";
    private static final String CARD_VAR = "Card";
    private static final Atom TEST_NS = ASSyntax.createAtom("kb");
    private final Literal key;
    private final Literal value;
    private Proposition currentProposition;
    private Proposition clonedProposition;


    public ValidPropositionTest(Literal key, Literal value) {
        this.key = key;
        this.value = value;
    }

    @Before
    public void setUp() throws Exception {
        this.currentProposition = new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
        this.clonedProposition = new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
    }

    public static Literal createLiteral(Term first, Term second) {
        return ASSyntax.createLiteral(TEST_FUNCTOR, first, second);
    }

    public static Literal createLiteral(String first, String second) {
        return createLiteral(ASSyntax.createString(first), ASSyntax.createString(second));
    }

    public static Literal createLiteral(String first) {
        return createLiteral(ASSyntax.createString(first), ASSyntax.createVar(CARD_VAR));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getTestData() {
        return Arrays.asList(new Object[][]{
                {
                        createLiteral("Alice"),
                        createLiteral("Alice", "AA")
                },
                {
                        createLiteral("Alice").cloneNS(TEST_NS),
                        createLiteral("Alice", "AA")
                },
                {
                        createLiteral("Alice"),
                        createLiteral("Alice", "AA").cloneNS(TEST_NS)

                },
                {
                        createLiteral("Alice").cloneNS(TEST_NS),
                        createLiteral("Alice", "AA").cloneNS(TEST_NS)
                }
        });
    }

    @Test
    public void getKey() {
        assertNotNull("key should not be null", currentProposition.getKey());
        assertTrue("key should be normalized", currentProposition.getKey().isNormalized());
    }

    @Test
    public void getValue() {
        assertNotNull("value should not be null", currentProposition.getValue());
        assertTrue("value should be normalized", currentProposition.getValue().isNormalized());
        assertTrue("value should be ground", currentProposition.getValue().getOriginalLiteral().isGround());
    }

    @Test
    public void getKeyLiteral() {
        assertNotNull("key literal should not be null", currentProposition.getKeyLiteral());
        assertEquals("key literal should not be the same as the original wrapped key literal", currentProposition.getKeyLiteral(), currentProposition.getKey().getOriginalLiteral());
        assertTrue("key literal should be normalized", new WrappedLiteral(currentProposition.getKeyLiteral()).isNormalized());
    }

    @Test
    public void getValueLiteral() {
        assertNotNull("value literal should not be null", currentProposition.getValueLiteral());
        assertEquals("value literal should not be the same as the original wrapped value literal", currentProposition.getValueLiteral(), currentProposition.getValue().getOriginalLiteral());
        assertTrue("value literal should be normalized", new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized());
    }

    @Test
    public void setValue() {

    }

    @Test
    public void testEquals() {
        assertEquals("clone should equal current prop", clonedProposition, currentProposition);
    }

    @Test
    public void testHashCode() {
        assertEquals("clone hash should equal current prop hash", clonedProposition.hashCode(), currentProposition.hashCode());
    }
}