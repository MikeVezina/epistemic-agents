package epistemic.distribution.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;

public class KnowEpistemicFormula extends EpistemicFormula {
    /**
     * Can only be constructed through the static parseLiteral method.
     *
     * @param originalLiteral The original literal corresponding to this epistemic formula
     */
    public KnowEpistemicFormula(Literal originalLiteral) {
        super(EpistemicModality.KNOW, originalLiteral);
    }

    /**
     * @return Knowledge formulas parsed from a literal will never have a negated modality.
     */
    @Override
    protected boolean getModalityNegated() {
        return false;
    }

    @Override
    protected WrappedLiteral processRootLiteral(WrappedLiteral originalLiteral) {
        return originalLiteral;
    }
}
