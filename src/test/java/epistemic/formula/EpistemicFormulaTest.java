package epistemic.formula;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

public abstract class EpistemicFormulaTest {

    @Before
    public void setUp() throws Exception {
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
    public void parseLiteral() {
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