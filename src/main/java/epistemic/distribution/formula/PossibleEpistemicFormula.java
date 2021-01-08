package epistemic.distribution.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;

public class PossibleEpistemicFormula extends EpistemicFormula {
    /**
     * Can only be constructed through the static parseLiteral method.
     *
     * @param originalLiteral The original literal corresponding to this epistemic formula
     */
    protected PossibleEpistemicFormula(Literal originalLiteral) {
        super(EpistemicModality.POSSIBLE, originalLiteral);

        // Assert arity == 1 and that nested term is literal.
        if(originalLiteral.getArity() != 1 || !originalLiteral.getTerm(0).isLiteral())
            throw new RuntimeException("Invalid Possible formula: " + originalLiteral);

    }

    @Override
    protected boolean getModalityNegated() {
        return getOriginalWrappedLiteral().getOriginalLiteral().negated();
    }

    @Override
    protected WrappedLiteral processRootLiteral(WrappedLiteral originalLiteral) {
        return new WrappedLiteral((Literal) originalLiteral.getOriginalLiteral().getTerm(0));
    }
}
