package epistemic.distribution.generator;

import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;

import java.util.Iterator;

public interface LogicalConsequenceCallback {
    Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u);
}
