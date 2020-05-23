package epistemic.formula;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * This class is used to represent a knowledge formula.
 * For example, a literal 'know(know(hello))' will get unwrapped so that we have access
 * to the rootLiteral (i.e. hello) as well as the chain of embedded literals.
 */
public class EpistemicFormula {
    private WrappedLiteral rootLiteral;
    private WrappedLiteral originalLiteral;
    private EpistemicFormula nextLiteral;


    private EpistemicFormula(Literal originalLiteral) {
        this.originalLiteral = new WrappedLiteral(originalLiteral);
    }

    /**
     * @return True if the next literal is the root literal, false otherwise.
     */
    public boolean isNextElementRoot()
    {
        return nextLiteral != null && nextLiteral.rootLiteral.equals(rootLiteral);
    }

    public static EpistemicFormula parseLiteral(Literal currentLiteral) {

        var rootEpiLiteral = new EpistemicFormula(currentLiteral.copy());

        if (!isEpistemicLiteral(currentLiteral)) {

            rootEpiLiteral.rootLiteral = new WrappedLiteral(currentLiteral);
            rootEpiLiteral.nextLiteral = null;

            return rootEpiLiteral;
        }

        Term nestedTerm = currentLiteral.getTerm(0);

        if (!(nestedTerm instanceof Literal))
            throw new IllegalArgumentException("currentLiteral (" + currentLiteral + ") does not have a nested literal term");

        rootEpiLiteral.setNextLiteral(parseLiteral((Literal) nestedTerm));
        return rootEpiLiteral;
    }


    public WrappedLiteral getRootLiteral() {
        return rootLiteral;
    }

    public EpistemicFormula getNextLiteral() {
        return nextLiteral;
    }

    private void setNextLiteral(EpistemicFormula nextLiteral) {
        this.rootLiteral = nextLiteral.rootLiteral;
        this.nextLiteral = nextLiteral;
    }

    public static boolean isEpistemicLiteral(Literal curLit) {
        return curLit != null && curLit.getFunctor().equals("know");
    }

    public Literal getOriginalLiteral() {
        return originalLiteral.getLiteral();
    }

    @Override
    public int hashCode() {
        return originalLiteral.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}