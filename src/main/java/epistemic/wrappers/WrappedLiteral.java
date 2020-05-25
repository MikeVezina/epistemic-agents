package epistemic.wrappers;

import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.List;

/**
 * Provides a wrapper for Literals.
 * Overrides the hash & equals methods so that term variables provide the same hashcode and that hand(_) is equivalent to hand(Var).
 * Provides a method ("toSafePropName") that converts the literal to a proposition.
 */
public class WrappedLiteral {

    private final Literal literalOriginal;
    private final Literal modifiedLiteral;

    public WrappedLiteral(Literal literal) {
        this.literalOriginal = literal;
        this.modifiedLiteral = createModifiedLiteral().copy();
        replaceTerms();
    }

    /**
     * Creates a modified literal. Removes any annotations, namespaces, and negations.
     * @return The modified literal object.
     */
    protected Literal createModifiedLiteral() {
        return ((Literal) literalOriginal.clearAnnots().cloneNS(Literal.DefaultNS));
    }

    /**
     * Wraps all of the terms so that the necessary functions are overridden.
     */
    private void replaceTerms() {
        List<Term> termList = modifiedLiteral.getTerms();

        if (termList == null)
            return;

        for (int i = 0; i < termList.size(); i++)
            modifiedLiteral.setTerm(i, new WrappedTerm(termList.get(i)));
    }

    /**
     * Returns true if this object and wrapped literal can be unified (either this object gets unified by the wrappedLiteral, or vice-versa).
     *
     * @param wrappedLiteral The wrapped literal to check for unification
     * @return
     */
    public boolean canUnify(WrappedLiteral wrappedLiteral) {
        var unifier = new Unifier();
        return unifier.unifies(this.getOriginalLiteral(), wrappedLiteral.getOriginalLiteral());
    }

    /**
     * Calculates a hash code for the literal. This utilizes a similar hashing function to the one implemented by LiteralImpl, with the only difference being how the terms are hashed.
     * Variable terms always provide a constant hash value and are no longer hashed based on their respective hashcode.
     * <p>
     * For example, this means that the following literals will calculate the same hashcode:
     * literal(Test, "asd")
     * literal(Wow, "asd")
     * literal(_, "asd")
     *
     * TODO: right now this also includes a hack to adjust the hash code for negated literals (hash collision, see issue {@link 'https://github.com/jason-lang/jason/issues/40'})
     * @return The modified hashcode for the literal
     */
    @Override
    public int hashCode() {
        return modifiedLiteral.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WrappedLiteral) {
            WrappedLiteral other = (WrappedLiteral) o;
            return modifiedLiteral.equals(other.modifiedLiteral);
        }

        return modifiedLiteral.equals(o);
    }

    /**
     * Consider a URL-safe name
     *
     * @return
     */
    public String toSafePropName() {
        StringBuilder propName = new StringBuilder();
        propName.append(literalOriginal.getFunctor());

        var terms = literalOriginal.getTerms();

        if (!terms.isEmpty()) {
            propName.append("_");

            for (Term term : literalOriginal.getTerms()) {
                propName.append(term.toString().replace("(", "[").replace(")", "]"));
                propName.append("_");
            }
        }


        return propName.toString();
    }

    public WrappedLiteral copy() {
        return new WrappedLiteral(this.literalOriginal.copy());
    }

    public Literal getOriginalLiteral() {
        return this.literalOriginal;
    }

    /**
     * @return a wrapped literal that has been cloned to contain the default namespace, removes any negation and annotations.
     */
    public NormalizedWrappedLiteral getNormalizedWrappedLiteral()
    {
        return new NormalizedWrappedLiteral(this.getOriginalLiteral());
    }

    protected static Literal getNormalizedLiteral(Literal originalLiteral)
    {
        return ((LiteralImpl) originalLiteral.clearAnnots().cloneNS(Literal.DefaultNS)).setNegated(Literal.LPos);
    }

    public PredicateIndicator getPredicateIndicator() {
        return modifiedLiteral.getPredicateIndicator();
    }

    @Override
    public String toString() {
        return "LiteralKey{" +
                modifiedLiteral +
                '}';
    }

    public boolean isNormalized() {
        return !literalOriginal.hasAnnot() && literalOriginal.getNS().equals(Literal.DefaultNS) && !literalOriginal.negated();
    }
}


