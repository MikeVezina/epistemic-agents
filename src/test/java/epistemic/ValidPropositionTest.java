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
public class ValidPropositionTest extends PropositionTest {
    private Proposition currentProposition;
    private Proposition clonedProposition;


    public ValidPropositionTest(Literal key, Literal value) {
        super(key, value);
    }

    @Before
    public void setUp() throws Exception {
        this.currentProposition = new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
        this.clonedProposition = new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
    }

    @Parameterized.Parameters(name = "Key: {0}, Value: {1}")
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
        currentProposition.setValue(new WrappedLiteral(createLiteral("Bob", "88")));
        assertTrue("set value should be rejected since it doesnt unify with the key", new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized());
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