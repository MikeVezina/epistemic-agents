package epistemic.agent;

import epistemic.EpistemicDistribution;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;
import jason.bb.DefaultBeliefBase;

import java.util.ArrayList;
import java.util.Iterator;

public class ChainedEpistemicBB extends ChainBBAdapter {
    private final EpistemicDistribution epistemicDistribution;
    private final EpistemicAgent epistemicAgent;

    public ChainedEpistemicBB(EpistemicAgent agent, EpistemicDistribution distribution) {
        this(agent.getBB(), agent, distribution);
    }

    public ChainedEpistemicBB(BeliefBase beliefBase, EpistemicAgent agent, EpistemicDistribution distribution) {
        super(beliefBase != null ? beliefBase : new DefaultBeliefBase());
        this.epistemicDistribution = distribution;
        this.epistemicAgent = agent;
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        return super.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        // Copy & Apply the unifier to the literal
        Literal unifiedLiteral = (Literal) l.capply(u);

        if (!EpistemicFormula.isEpistemicLiteral(l))
            return super.getCandidateBeliefs(l, u);

        var epistemicLiteral = EpistemicFormula.parseLiteral(unifiedLiteral);

        if (epistemicLiteral == null) {
            System.out.println("Failed to create epistemic literal from unified belief: " + unifiedLiteral);
            return super.getCandidateBeliefs(l, u);
        }

        // If the literal is not managed by us, we delegate to the chained BB.
        if (!epistemicDistribution.getManagedWorlds().getManagedLiterals().isManagedBelief(epistemicLiteral.getRootLiteral().getPredicateIndicator())) {
            System.out.println("The root literal " + epistemicLiteral.getOriginalLiteral() + " is not managed by the reasoner.");
            return super.getCandidateBeliefs(l, u);
        }

        // If the root literal is not ground, then obtain all possible managed unifications
        var groundFormulas = epistemicAgent.getCandidateFormulas(epistemicLiteral);

        var result = epistemicDistribution.evaluateFormulas(groundFormulas);
        var arr = new ArrayList<Literal>();

        // If the result is true (formula evaluated to true), then return the literal as a candidate belief
        for(var formulaResultEntry : result.entrySet()) {

            // Add formula literal to results list if the formula was evaluated to true
            if(formulaResultEntry.getValue())
                arr.add(formulaResultEntry.getKey().getOriginalLiteral());
        }

        // Return null if no candidates
        // This maintains the original BB functionality
        if(arr.isEmpty())
            return null;

        return arr.iterator();
    }



}
