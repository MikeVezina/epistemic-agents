package epistemic.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to represent a knowledge formula.
 * For example, a literal 'know(know(hello))' will get unwrapped so that we have access
 * to the rootLiteral (i.e. hello) as well as the chain of embedded literals.
 */
public class EpistemicFormula {

    public enum EpistemicFormulaFunctor {
        KNOW("know"),
        POSSIBLE("possible");

        private final Set<String> functorSet;
        EpistemicFormulaFunctor(String... functors) {
            this.functorSet = new HashSet<>();
            this.functorSet.addAll(Arrays.asList(functors));
        }

        public boolean isFunctor(String functor)
        {
            return this.functorSet.contains(functor);
        }

        /**
         * Finds the first functor enum value with the given functor.
         * @param functor The string functor to look for in enum values.
         * @return The first corresponding enum value or null if the functor could not be found.
         */
        public static EpistemicFormulaFunctor findFunctor(String functor)
        {
            for(EpistemicFormulaFunctor functorVal : EpistemicFormulaFunctor.values())
            {
                if(functorVal.isFunctor(functor))
                    return functorVal;
            }
            return null;
        }
    }
    private WrappedLiteral rootLiteral;
    private final WrappedLiteral originalLiteral;
    private EpistemicFormula nextLiteral;


    /**
     * Can only be constructed through the static parseLiteral method.
     *
     * @param originalLiteral The original literal corresponding to this epistemic formula
     */
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

    /**
     * Recursively parses a literal into an epistemic formula. If the literal is not
     * an epistemic literal, an EpistemicFormula object will not be created and
     * null will be returned. Calls {@link EpistemicFormula#isEpistemicLiteral(Literal)}
     * to check if literal is epistemic.
     *
     * @see EpistemicFormula#isEpistemicLiteral(Literal)
     *
     * @param originalLiteral The epistemic literal to be converted into an EpistemicFormula object.
     * @return An EpistemicFormula object parsed from the literal. Null if the literal is not epistemic.
     */
    public static EpistemicFormula parseLiteral(Literal originalLiteral)
    {
        return isEpistemicLiteral(originalLiteral) ? parseLiteralRecursive(originalLiteral) : null;
    }

    private static EpistemicFormula parseLiteralRecursive(Literal currentLiteral) {

        var currentEpistemicFormula = new EpistemicFormula(currentLiteral.copy());

        if (!isEpistemicLiteral(currentLiteral)) {

            currentEpistemicFormula.rootLiteral = new WrappedLiteral(currentLiteral);
            currentEpistemicFormula.nextLiteral = null;

            return currentEpistemicFormula;
        }

        Term nestedTerm = currentLiteral.getTerm(0);

        if (!(nestedTerm instanceof Literal))
            throw new IllegalArgumentException("currentLiteral (" + currentLiteral + ") does not have a nested literal term");

        currentEpistemicFormula.setNextLiteral(parseLiteralRecursive((Literal) nestedTerm));
        return currentEpistemicFormula;
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
        return curLit != null && EpistemicFormulaFunctor.findFunctor(curLit.getFunctor()) != null;
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