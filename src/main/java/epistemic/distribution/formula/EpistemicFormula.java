package epistemic.distribution.formula;

import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to represent a knowledge formula.
 * For example, a literal 'know(know(hello))' will get unwrapped so that we have access
 * to the rootLiteral (i.e. hello) as well as the chain of embedded literals.
 */
public abstract class EpistemicFormula {

    private final WrappedLiteral rootLiteral;
    private final WrappedLiteral originalLiteral;
    private final EpistemicModality modality;
    private final boolean modalityNegated;
    private final boolean propositionNegated;
    private final UUID uuid;

    /**
     * Can only be constructed through the static parseLiteral method.
     *
     * @param originalLiteral The original literal corresponding to this epistemic formula
     */
    protected EpistemicFormula(EpistemicModality modality, Literal originalLiteral) {
        this.modality = modality;
        this.originalLiteral = new WrappedLiteral(originalLiteral);
        this.rootLiteral = processRootLiteral(this.originalLiteral);
        this.modalityNegated = getModalityNegated();
        this.propositionNegated = rootLiteral.getOriginalLiteral().negated();;
        uuid = UUID.randomUUID();
    }

    protected abstract boolean getModalityNegated();

    protected abstract WrappedLiteral processRootLiteral(WrappedLiteral originalLiteral);

    public boolean isPropositionNegated() {
        return propositionNegated;
    }

    public boolean isModalityNegated() {
        return modalityNegated;
    }

    /**
     * Recursively parses a literal into an epistemic formula. If the literal is not
     * an epistemic literal, an EpistemicFormula object will not be created and
     * null will be returned. Calls {@link EpistemicFormula#isEpistemicLiteral(Literal)}
     * to check if literal is epistemic.
     *
     * @param originalLiteral The epistemic literal to be converted into an EpistemicFormula object.
     * @return An EpistemicFormula object parsed from the literal. Null if the literal is not epistemic.
     * @see EpistemicFormula#isEpistemicLiteral(Literal)
     */
    public static EpistemicFormula fromLiteral(Literal originalLiteral) {
        Literal copyOriginal = originalLiteral.copy();

        if(EpistemicModality.POSSIBLE.isFunctor(copyOriginal.getFunctor()))
            return new PossibleEpistemicFormula(copyOriginal);
        else
            return new KnowEpistemicFormula(copyOriginal);
    }

    public EpistemicModality getEpistemicModality() {
        return modality;
    }

    public WrappedLiteral getRootLiteral() {
        return rootLiteral;
    }

    @Deprecated
    private static boolean isEpistemicLiteral(Literal curLit) {
        return curLit != null && EpistemicModality.findFunctor(curLit.getFunctor()) != null && curLit.getArity() == 1;
    }

    public String getUniqueId()
    {
        return this.uuid.toString();
    }

    public Literal getCleanedOriginal() {
        return originalLiteral.getCleanedLiteral();
    }

    public WrappedLiteral getOriginalWrappedLiteral() {
        return originalLiteral;
    }

    /**
     * Applies the unifier to the current formula
     * @param unifier The unifier with the corresponding variable values
     * @return A new epistemic formula object with the unified values.
     */
    public EpistemicFormula capply(Unifier unifier)
    {
        Literal applied = (Literal) getCleanedOriginal().capply(unifier);
        applied.resetHashCodeCache();
        return EpistemicFormula.fromLiteral(applied);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof EpistemicFormula)) return false;
        EpistemicFormula that = (EpistemicFormula) o;
        return modalityNegated == that.modalityNegated && propositionNegated == that.propositionNegated && rootLiteral.equals(that.rootLiteral) && modality == that.modality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootLiteral.toSafePropName(), modality, modalityNegated, propositionNegated);
    }

    @Override
    public String toString() {
        return getCleanedOriginal().toString();
    }

    public String getAtomicProposition() {
        return rootLiteral.toSafePropName();
    }
}