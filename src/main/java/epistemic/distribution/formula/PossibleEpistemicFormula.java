package epistemic.distribution.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

public class PossibleEpistemicFormula extends EpistemicFormula {
    /**
     * @param originalLiteral The original literal corresponding to this epistemic formula
     */
    public PossibleEpistemicFormula(Literal originalLiteral) {
        super(EpistemicModality.POSSIBLE, originalLiteral);

        // Assert arity == 1 and that nested term is literal.
        if (originalLiteral.getArity() != 1 || !originalLiteral.getTerm(0).isLiteral())
            throw new RuntimeException("Invalid Possible formula: " + originalLiteral);

    }

    public EpistemicFormula deriveNewPossibleFormula(boolean modalNegated, boolean propNegated) {
        return EpistemicFormula.fromLiteral(
                ASSyntax.createLiteral(
                        EpistemicModality.POSSIBLE.getFunctor(),
                        getRootLiteral().getCleanedLiteral().setNegated(propNegated ? Literal.LNeg : Literal.LPos))
                        .setNegated(modalNegated ? Literal.LNeg : Literal.LPos)
        );
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
