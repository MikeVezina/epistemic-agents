package epistemic.wrappers;

import jason.asSyntax.Literal;

public class NormalizedWrappedLiteral extends WrappedLiteral {
    public NormalizedWrappedLiteral(Literal literal) {
        super(getNormalizedLiteral(literal));
    }
}
