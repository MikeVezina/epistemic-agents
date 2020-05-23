package jason;

import epistemic.EpistemicDistribution;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;

import java.util.ArrayList;
import java.util.Iterator;

public class ChainedEpistemicBB extends ChainBBAdapter {
    private final EpistemicDistribution epistemicDistribution;
    public ChainedEpistemicBB(BeliefBase bb, EpistemicDistribution distribution) {
        super(bb);
        this.epistemicDistribution = distribution;
    }

    @Override
    public Literal contains(Literal l) {
        return super.contains(l);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        return super.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        // Copy & Apply the unifier to the literal
        Literal groundLiteral = (Literal) l.capply(u);

        if(!groundLiteral.isGround())
            System.out.println(groundLiteral + " is not ground after unifying");

        if(!groundLiteral.isGround() || !EpistemicFormula.isEpistemicLiteral(l))
            return super.getCandidateBeliefs(l, u);


        var epistemicLiteral = EpistemicFormula.parseLiteral(groundLiteral);

        // If the literal is not managed by us, we delegate to the chained BB.
        if(!epistemicDistribution.getManagedWorlds().isManagedBelief(epistemicLiteral.getRootLiteral()))
            return super.getCandidateBeliefs(l, u);

        var result = epistemicDistribution.evaluateFormula(epistemicLiteral);
        var arr = new ArrayList<Literal>();

        // If the result is true (formula evaluated to true), then return the literal as a candidate belief
        if(result)
            arr.add(groundLiteral);

        return arr.iterator();
    }

}
