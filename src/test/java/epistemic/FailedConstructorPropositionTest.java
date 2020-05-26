package epistemic;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FailedConstructorPropositionTest extends PropositionTest {

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    public FailedConstructorPropositionTest(Literal key, Literal value) {
        super(key, value);
    }

    @Test
    public void failToUnify()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The literalValue can not unify the literalKey. Failed to create Proposition.");

        new Proposition(new WrappedLiteral(key), new WrappedLiteral(value));
    }

    @Test
    @Parameterized.Parameter
    public void valueNotGround()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("literalValue is not ground");

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