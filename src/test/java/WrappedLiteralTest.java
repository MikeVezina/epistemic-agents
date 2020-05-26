import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class WrappedLiteralTest {

    public static Literal NO_TERMS = ASSyntax.createLiteral("test");
    public static Literal NO_TERMS_COMPARE = ASSyntax.createLiteral("test2");

    public static Literal ONE_TERM_NO_VARS = ASSyntax.createLiteral("test", new StringTermImpl("value"));
    public static Literal ONE_TERM_NO_VARS_COMPARE = ASSyntax.createLiteral("test", new StringTermImpl("values"));

    public static Literal ONE_TERM_UNNAMED_VAR = ASSyntax.createLiteral("test", ASSyntax.createVar());
    public static Literal ONE_TERM_UNNAMED_VAR_COMPARE = ASSyntax.createLiteral("test", ASSyntax.createVar());
    public static Literal ONE_TERM_NAMED_VAR = ASSyntax.createLiteral("test", ASSyntax.createVar());

    private static final Literal TWO_TERMS_NO_VARS_COMPARE = ASSyntax.createLiteral("test", new StringTermImpl("value"), new StringTermImpl("value2"));
    public static Literal TWO_TERMS_NO_VARS = ASSyntax.createLiteral("test", new StringTermImpl("value"), new StringTermImpl("value2"));

    public static Literal TWO_TERMS_ONE_UNNAMED = ASSyntax.createLiteral("test", new StringTermImpl("value"), new StringTermImpl("value2"));


    @Before
    public void setUp() {

    }


    @Test
    public void testHashCodeNoTerms() {
        WrappedLiteral wrappedLiteral = new WrappedLiteral(NO_TERMS);
        WrappedLiteral literalCopy = new WrappedLiteral(NO_TERMS.copy());
        WrappedLiteral literalCompare = new WrappedLiteral(NO_TERMS_COMPARE);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(wrappedLiteral, literalCopy);
        assertNotEqualsHash(wrappedLiteral, literalCompare);

    }


    @Test
    public void testHashCodeOneTerm() {
        WrappedLiteral wrappedLiteral = new WrappedLiteral(ONE_TERM_NO_VARS);
        WrappedLiteral literalCopy = new WrappedLiteral(ONE_TERM_NO_VARS.copy());
        WrappedLiteral literalCompare = new WrappedLiteral(ONE_TERM_NO_VARS_COMPARE);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(wrappedLiteral, literalCopy);
        assertNotEqualsHash(wrappedLiteral, literalCompare);
    }

    @Test
    public void testHashCodeOneVarTerm() {

        // Assert that our fixtures are correct. The terms should not be equivalent (according to compareTo)
        assertNotEquals(0, ONE_TERM_UNNAMED_VAR.getTerm(0).compareTo(ONE_TERM_UNNAMED_VAR_COMPARE.getTerm(0)));

        WrappedLiteral wrappedLiteral = new WrappedLiteral(ONE_TERM_UNNAMED_VAR);
        WrappedLiteral literalCopy = new WrappedLiteral(ONE_TERM_UNNAMED_VAR_COMPARE);

        WrappedLiteral literalCompare = new WrappedLiteral(ONE_TERM_NAMED_VAR);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(wrappedLiteral, literalCopy);
        assertEqualsHash(wrappedLiteral, literalCompare);
    }


    private static WrappedLiteral createKey(Literal literal)
    {
        return new WrappedLiteral(literal);
    }

    private static WrappedLiteral createKey(String litString)
    {
        try {
            return new WrappedLiteral(ASSyntax.parseLiteral(litString));
        } catch (ParseException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    @Test
    public void testHashCodeTwoVarTerms() {
        WrappedLiteral key = createKey("test(_, Test)");

        // These are the same as key
        WrappedLiteral keyCopy = key.copy();
        WrappedLiteral keyUnnamed = createKey("test(First, _)");
        WrappedLiteral keyUnnamedTwo = createKey("test(Test, _)");
        WrappedLiteral keyBothNamed= createKey("test(Test, Second)");
        WrappedLiteral keyBothUnnamed = createKey("test(_, _)");

        assertEqualsHash(key, keyCopy);
        assertEqualsHash(key, keyUnnamed);
        assertEqualsHash(key, keyUnnamedTwo);
        assertEqualsHash(key, keyBothNamed);
        assertEqualsHash(key, keyBothUnnamed);

        // These are not the same as key
        WrappedLiteral keyHasMissingTerm = createKey("test(_)");
        WrappedLiteral keyHasVal = createKey("test(_, asd)");
        WrappedLiteral keyHasVal2 = createKey("test(asd, _)");
        WrappedLiteral keyHasVals = createKey("test(asd, asd)");

        assertNotEqualsHash(key, keyHasMissingTerm);
        assertNotEqualsHash(key, keyHasVal);
        assertNotEqualsHash(key, keyHasVal2);
        assertNotEqualsHash(key, keyHasVals);
    }

    private void assertEqualsHash(WrappedLiteral actual, WrappedLiteral expected)
    {
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
    }

    private void assertNotEqualsHash(WrappedLiteral actual, WrappedLiteral expected)
    {
        assertNotEquals(expected.hashCode(), actual.hashCode());
        assertNotEquals(expected, actual);
    }


    @Test
    public void testHashCodeTwoTermsOneVarTerm() {

        WrappedLiteral key = createKey("test(asd, Test)");
        var litOne = ASSyntax.createLiteral("test", ASSyntax.createString("Test"), ASSyntax.createAtom("asd"));
        var litTwo = ASSyntax.createLiteral("test", ASSyntax.createAtom("asd"), ASSyntax.createString("Test"));
        var hashOne = litOne.hashCode();
        var hashTwo = litTwo.hashCode();

        // These are the same as key
        assertEqualsHash(key, key.copy());
        assertEqualsHash(key, createKey("test(asd, _)"));
        assertEqualsHash(key, createKey("test(asd, Wow)"));
        assertEqualsHash(key, createKey("test(asd, _)"));

        // These are not the same as key
        assertNotEqualsHash(key, createKey("test(asd)"));
        assertNotEqualsHash(key, createKey("test(asd, asd)"));
        assertNotEqualsHash(key, createKey("test(Test, asd)"));
        assertNotEqualsHash(key, createKey("test(_, asd)"));
        assertNotEqualsHash(key, createKey("test(asd, asd)"));
    }
}
