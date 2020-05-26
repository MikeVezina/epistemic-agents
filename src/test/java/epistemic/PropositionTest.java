package epistemic;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public abstract class PropositionTest {
    protected static final String TEST_FUNCTOR = "hand";
    protected static final String CARD_VAR = "Card";
    protected static final Atom TEST_NS = ASSyntax.createAtom("kb");

    protected final Literal key;
    protected final Literal value;

    protected PropositionTest(Literal key, Literal value) {
        this.key = key;
        this.value = value;
    }

    protected static Literal createLiteral(Term first, Term second) {
        return ASSyntax.createLiteral(TEST_FUNCTOR, first, second);
    }

    protected static Literal createLiteral(String first, String second) {
        return createLiteral(ASSyntax.createString(first), ASSyntax.createString(second));
    }

    protected static Literal createLiteral(String first) {
        return createLiteral(ASSyntax.createString(first), ASSyntax.createVar(CARD_VAR));
    }
}
