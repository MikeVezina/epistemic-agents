package epistemic;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class UnificationFailedPropositionTest extends PropositionTest {


    public UnificationFailedPropositionTest(Literal key, Literal value) {
        super(key, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failToUnify()
    {
        new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
    }
    @Parameterized.Parameters(name = "Key: {0}, Value: {1}")
    public static Collection<Object[]> getTestData() {
        return Arrays.asList(new Object[][]{
                {
                        createLiteral("Bob"),
                        createLiteral("Alice", "AA")
                }
        });
    }


}