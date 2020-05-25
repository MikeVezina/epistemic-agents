package jason;

import epistemic.EpistemicDistribution;
import epistemic.formula.EpistemicFormula;
import epistemic.Proposition;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EpistemicAgent extends Agent {

    private EpistemicDistribution epistemicDistribution;

    public EpistemicAgent() {
        super.pl = new EpistemicPlanLibraryProxy(new PlanLibrary());
    }

    @Override
    public void load(String asSrc) throws JasonException {
        super.load(asSrc);
        this.agentLoaded();
    }

    @Override
    public void load(InputStream in, String sourceId) throws JasonException {
        super.load(in, sourceId);
        this.agentLoaded();
    }

    @Override
    public void initAg() {
        super.initAg();

    }

    @Override
    public void setPL(PlanLibrary pl) {
        // Ensure we always have a proxy plan library in place
        if(!(pl instanceof EpistemicPlanLibraryProxy))
            pl = new EpistemicPlanLibraryProxy(pl);

        super.setPL(pl);
    }

    @Override
    public EpistemicPlanLibraryProxy getPL() {
        return (EpistemicPlanLibraryProxy) super.getPL();
    }

    /**
     * Called when the agent has been loaded and parsed from the source file.
     * The possible worlds distribution can be initialized here. (Once initial beliefs/rules are parsed)
     */
    private void agentLoaded() {
        System.out.println("Loaded");
        this.epistemicDistribution = new EpistemicDistribution(this);
        this.setBB(new ChainedEpistemicBB(this, this.epistemicDistribution));
    }

    @Override
    public boolean believes(LogicalFormula bel, Unifier un) {
        return super.believes(bel, un);
    }

    @Override
    public Literal findBel(Literal bel, Unifier un) {
        return super.findBel(bel, un);
    }

    /**
     * Update managed world props in this function. We can still operate on beliefs.
     * Use super.addBel(), etc. to trigger the BRF (and generate events) for inferred knowledge.
     */
    @Override
    public int buf(Collection<Literal> percepts) {
        // Update the BB with new percepts before updating the managed worlds
        int result = super.buf(percepts);

        if (this.epistemicDistribution != null)
            this.epistemicDistribution.buf(percepts, this.getPL().getSubscribedFormulas());

        return result;
    }

    /**
     * Controls the addition / deletion of a belief. This function gets executed when +belief, -belief, or -+belief is executed by the ExecInt function.
     * <p>
     * We need to confirm that the model is correct. i.e. all proposition keys should have one (and only one) corresponding value in the BB.
     *
     * @see Agent#brf(Literal, Literal, Intention)
     */
    @Override
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {

        if (this.epistemicDistribution != null)
            this.epistemicDistribution.brf(beliefToAdd, beliefToDel);

        return super.brf(beliefToAdd, beliefToDel, i);
    }


    /**
     * Creates events for belief plans (+knows(knows(hello)))
     * @param newKnowledge
     */
    public void createKnowledgeEvent(Trigger.TEOperator operator, EpistemicFormula newKnowledge) {
        Trigger te = new Trigger(operator, Trigger.TEType.belief, newKnowledge.getOriginalLiteral());
        this.getTS().updateEvents(new Event(te, Intention.EmptyInt));
    }

    /**
     * Unifies all possible values to an ungrounded epistemic formula
     * (i.e. an epistemic formula with an ungrounded root literal)
     *
     * @param epistemicFormula The epistemic formula with an ungrounded root literal.
     * @return The set of all grounded literals that unify with epistemicFormula. This will
     * return an empty set if epistemicFormula is null.
     */
    public Set<EpistemicFormula> getCandidateFormulas(EpistemicFormula epistemicFormula) {
        Set<EpistemicFormula> groundFormulaSet = new HashSet<>();

        if (epistemicFormula == null)
            return groundFormulaSet;

        // If the root literal is already ground, return a set containing the ground formula.
        if(epistemicFormula.getRootLiteral().getOriginalLiteral().isGround())
        {
            groundFormulaSet.add(epistemicFormula);
            return groundFormulaSet;
        }

        // Obtain all possible proposition values from the managed literals object and attempt to unify each value with the ungrounded epistemic formula.
        // All formulas that can be successfully ground will be added to the set.

        // TODO: This has issues. finding by predicate indicator does not incorporate negation in the way that we'd like (i.e. ignore it)
        for(Proposition managedValue : epistemicDistribution.getManagedWorlds().getManagedLiterals().getManagedBeliefs(epistemicFormula.getRootLiteral().getPredicateIndicator()))
        {
            Unifier unifier = new Unifier();

            // Create a cloned/normalized & ungrounded root literal to unify with
            var ungroundedLiteral = epistemicFormula.getRootLiteral().getNormalizedWrappedLiteral();
            var managedLiteral = managedValue.getValue().getNormalizedWrappedLiteral();

            // Attempt to unify with the various managed propositions
            if(unifier.unifiesNoUndo(managedLiteral.getOriginalLiteral(), ungroundedLiteral.getOriginalLiteral()))
            {
                var unifiedFormula = epistemicFormula.capply(unifier);

                if(!unifiedFormula.getOriginalLiteral().isGround()) {
                    System.out.println("Formula is still not ground after unifying: " + unifiedFormula);
                    continue;
                }

                groundFormulaSet.add(unifiedFormula);
            }
        }

        return groundFormulaSet;
    }
}
