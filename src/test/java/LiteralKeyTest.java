import wrappers.LiteralKey;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class LiteralKeyTest {

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
        LiteralKey literalKey = new LiteralKey(NO_TERMS);
        LiteralKey literalCopy = new LiteralKey(NO_TERMS.copy());
        LiteralKey literalCompare = new LiteralKey(NO_TERMS_COMPARE);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(literalKey, literalCopy);
        assertNotEqualsHash(literalKey, literalCompare);

    }


    @Test
    public void testHashCodeOneTerm() {
        LiteralKey literalKey = new LiteralKey(ONE_TERM_NO_VARS);
        LiteralKey literalCopy = new LiteralKey(ONE_TERM_NO_VARS.copy());
        LiteralKey literalCompare = new LiteralKey(ONE_TERM_NO_VARS_COMPARE);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(literalKey, literalCopy);
        assertNotEqualsHash(literalKey, literalCompare);
    }

    @Test
    public void testHashCodeOneVarTerm() {

        // Assert that our fixtures are correct. The terms should not be equivalent (according to compareTo)
        assertNotEquals(0, ONE_TERM_UNNAMED_VAR.getTerm(0).compareTo(ONE_TERM_UNNAMED_VAR_COMPARE.getTerm(0)));

        LiteralKey literalKey = new LiteralKey(ONE_TERM_UNNAMED_VAR);
        LiteralKey literalCopy = new LiteralKey(ONE_TERM_UNNAMED_VAR_COMPARE);

        LiteralKey literalCompare = new LiteralKey(ONE_TERM_NAMED_VAR);

        // Two literals with the same functor should have the same hashcode
        assertEqualsHash(literalKey, literalCopy);
        assertEqualsHash(literalKey, literalCompare);
    }


    private static LiteralKey createKey(Literal literal)
    {
        return new LiteralKey(literal);
    }

    private static LiteralKey createKey(String litString)
    {
        try {
            return new LiteralKey(ASSyntax.parseLiteral(litString));
        } catch (ParseException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    @Test
    public void testHashCodeTwoVarTerms() {
        LiteralKey key = createKey("test(_, Test)");

        // These are the same as key
        LiteralKey keyCopy = key.copy();
        LiteralKey keyUnnamed = createKey("test(First, _)");
        LiteralKey keyUnnamedTwo = createKey("test(Test, _)");
        LiteralKey keyBothNamed= createKey("test(Test, Second)");
        LiteralKey keyBothUnnamed = createKey("test(_, _)");

        assertEqualsHash(key, keyCopy);
        assertEqualsHash(key, keyUnnamed);
        assertEqualsHash(key, keyUnnamedTwo);
        assertEqualsHash(key, keyBothNamed);
        assertEqualsHash(key, keyBothUnnamed);

        // These are not the same as key
        LiteralKey keyHasMissingTerm = createKey("test(_)");
        LiteralKey keyHasVal = createKey("test(_, asd)");
        LiteralKey keyHasVal2 = createKey("test(asd, _)");
        LiteralKey keyHasVals = createKey("test(asd, asd)");

        assertNotEqualsHash(key, keyHasMissingTerm);
        assertNotEqualsHash(key, keyHasVal);
        assertNotEqualsHash(key, keyHasVal2);
        assertNotEqualsHash(key, keyHasVals);
    }

    private void assertEqualsHash(LiteralKey actual, LiteralKey expected)
    {
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
    }

    private void assertNotEqualsHash(LiteralKey actual, LiteralKey expected)
    {
        assertNotEquals(expected.hashCode(), actual.hashCode());
        assertNotEquals(expected, actual);
    }


    @Test
    public void testHashCodeTwoTermsOneVarTerm() {

        LiteralKey key = createKey("test(asd, Test)");

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
