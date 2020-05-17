package wrappers;

import jason.asSyntax.*;

import java.util.List;

/**
 * Provides a wrapper for Literals.
 * Overrides the hash & equals methods so that term variables provide the same hashcode and that hand(_) is equivalent to hand(Var).
 * Provides a method ("toSafePropName") that converts the literal to a proposition.
 */
public class WrappedLiteral {

    private final Literal literalOriginal;
    private final Literal literalCopy;

    public WrappedLiteral(Literal literal) {
        this.literalOriginal = literal;
        this.literalCopy = (Literal) literal.clearAnnots().cloneNS(Literal.DefaultNS);
        replaceTerms();
    }

    /**
     * Wraps all of the terms so that the necessary functions are overridden.
     */
    private void replaceTerms() {
        List<Term> termList = literalCopy.getTerms();

        if (termList == null)
            return;

        for (int i = 0; i < termList.size(); i++)
            literalCopy.setTerm(i, new WrappedTerm(termList.get(i)));
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
     * @return The modified hashcode for the literal
     */
    @Override
    public int hashCode() {
        return literalCopy.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WrappedLiteral) {
            WrappedLiteral other = (WrappedLiteral) o;
            return literalCopy.equals(other.literalCopy);
        }

        return literalCopy.equals(o);
    }

    /**
     * Consider a URL-safe name (
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

    private static int CalculateTermHashCode(Term... terms) {
        if (terms == null)
            return 0;

        int result = 1;

        for (Term element : terms)
            result = 31 * result + ((element == null || element.isVar()) ? 0 : element.hashCode());

        return result;
    }

    public WrappedLiteral copy() {
        return new WrappedLiteral(this.literalOriginal.copy());
    }

    public Literal getLiteral() {
        return this.literalOriginal;
    }

    @Override
    public String toString() {
        return "LiteralKey{" +
                literalCopy +
                '}';
    }
}


