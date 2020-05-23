package epistemic.formula;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import org.json.simple.JSONObject;

/**
 * This class is used to represent a knowledge formula.
 * For example, a literal 'know(know(hello))' will get unwrapped so that we have access
 * to the rootLiteral (i.e. hello) as well as the chain of embedded literals.
 */
public class EpistemicLiteral {
    private WrappedLiteral rootLiteral;
    private WrappedLiteral originalLiteral;
    private EpistemicLiteral nextLiteral;


    private EpistemicLiteral(Literal originalLiteral) {
        this.originalLiteral = new WrappedLiteral(originalLiteral);
    }

    public boolean isEpistemic()
    {
        return false;
//        return originalLiteral
    }

    public static EpistemicLiteral parseLiteral(Literal currentLiteral) {

        var rootEpiLiteral = new EpistemicLiteral(currentLiteral.copy());

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

    public EpistemicLiteral getNextLiteral() {
        return nextLiteral;
    }

    private void setNextLiteral(EpistemicLiteral nextLiteral) {
        this.rootLiteral = nextLiteral.rootLiteral;
        this.nextLiteral = nextLiteral;
    }

    public static boolean isEpistemicLiteral(Literal curLit) {
        return curLit != null && curLit.getFunctor().equals("know");
    }

    public Literal getOriginalLiteral() {
        return originalLiteral.getLiteral();
    }

    public JsonElement toFormulaJSON() {
        var jsonElement = new JsonObject();
        jsonElement.addProperty("id", originalLiteral.hashCode());
        jsonElement.addProperty("type", getOriginalLiteral().getFunctor());

        // If there is no next literal, return the safe prop name of the root value
        if(nextLiteral == null || nextLiteral.originalLiteral.equals(rootLiteral))
            jsonElement.addProperty("prop", rootLiteral.toSafePropName());
        else
            jsonElement.add("inner", nextLiteral.toFormulaJSON());

        return jsonElement;
    }
}