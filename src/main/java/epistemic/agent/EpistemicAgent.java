package epistemic.agent;

import epistemic.World;
import epistemic.distribution.EpistemicDistribution;
import epistemic.distribution.SyntaxDistributionBuilder;
import epistemic.distribution.formula.EpistemicFormula;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.bb.BeliefBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;

public class EpistemicAgent extends Agent implements DefaultCircumstanceListener {

    private EpistemicDistribution epistemicDistribution;
    private final SyntaxDistributionBuilder distributionBuilder;

    public EpistemicAgent() {
        this(new SyntaxDistributionBuilder());
    }

    public EpistemicAgent(@NotNull SyntaxDistributionBuilder distributionBuilder) {
        super.pl = new EpistemicPlanLibrary(new PlanLibrary());
        this.distributionBuilder = distributionBuilder;

    }

    @Override
    public void load(String asSrc) throws JasonException {
        super.load(asSrc);
        this.agentLoaded();
    }

    @Override
    public boolean addBel(Literal bel) throws RevisionFailedException {
        return super.addBel(bel);
    }

    @Override
    public void load(InputStream in, String sourceId) throws JasonException {
        super.load(in, sourceId);
        this.agentLoaded();
    }

    /**
     * Called when the agent has been loaded and parsed from the source file.
     * The possible worlds distribution can be initialized here. (Once initial beliefs/rules are parsed)
     */
    protected void agentLoaded() {
        getLogger().info("Epistemic framework initialized. Creating Epistemic Distribution:");

        // Create the distribution after loading the agent successfully
        this.epistemicDistribution = distributionBuilder.createDistribution(this);

        // This will wrap the BB with a chained epistemic BB now that the distribution is set.
        this.setBB(this.getBB());

        // Call the distribution agent loaded function
        epistemicDistribution.agentLoaded();
        this.getTS().getC().addEventListener(this);


    }

    public void rebuildDistribution() {
        logger.info("Rebuilding epistemic distribution");
        long initTime = System.nanoTime();

        // Create a new distribution and only grab the managed worlds
        // (keep our current distribution object, as it contains other data such as current props)
        this.epistemicDistribution.setUpdatedWorlds(distributionBuilder.createDistribution(this).getManagedWorlds());

        long endTime = System.nanoTime();
        logger.info("Rebuild time (ms): " + ((endTime - initTime) / 1000000));
    }

    @Override
    public void setPL(PlanLibrary pl) {
        // Ensure we always have a proxy plan library in place
        if (!(pl instanceof EpistemicPlanLibrary))
            pl = new EpistemicPlanLibrary(pl);

        super.setPL(pl);
    }


    /**
     * @return The current EpistemicDistribution. This will return null before {@link EpistemicAgent#agentLoaded()} is called.
     */
    @Nullable
    public EpistemicDistribution getEpistemicDistribution() {
        return epistemicDistribution;
    }

    @Override
    public EpistemicPlanLibrary getPL() {
        return (EpistemicPlanLibrary) super.getPL();
    }

    /**
     * Update managed world props in this function. We can still operate on beliefs.
     * Use super.addBel(), etc. to trigger the BRF (and generate events) for inferred knowledge.
     */
    @Override
    public int buf(Collection<Literal> percepts) {

        Set<Literal> newPercepts = new HashSet<>();
        // We have to track deletions ourselves
        Set<Literal> deletions = new HashSet<>();

        // Add current (old) percepts
        super.bb.getPercepts().forEachRemaining(deletions::add);

        // Update the BB with new percepts before updating the managed worlds
        int result = super.buf(percepts);

        if (percepts != null) {
            // Use processed percepts rather than passed-in percepts
            super.bb.getPercepts().forEachRemaining(newPercepts::add);
            // Remove all deletions that were maintained
            deletions.removeAll(newPercepts);
        } else {
            deletions.clear(); // Remove any old percepts deletions
            newPercepts = null;
        }

        var ev = ts.getC().getEvents();
        ev.removeIf(e -> e.getTrigger().getType() != Trigger.TEType.belief || !e.isExternal());
        if (this.epistemicDistribution != null)
            this.epistemicDistribution.buf(newPercepts, deletions, this.getPL().getSubscribedFormulas());

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
        var revisionResult = new RevisionResult();

        if (this.epistemicDistribution != null)
            revisionResult.addResult(this.epistemicDistribution.brf(beliefToAdd, beliefToDel));
        else
            return super.brf(beliefToAdd, beliefToDel, i);

        // Forward any revisions to the super class to add to / delete from the BB
        var superRevision = new RevisionResult();

        // Add all revised propositions to the BB and keep track of any further revisions
        for (var additions : revisionResult.getAdditions()) {
            superRevision.addResult(super.brf(additions, null, i));
        }

        // Add all revised propositions to the BB and keep track of any further revisions
        for (var deletion : revisionResult.getDeletions()) {
            superRevision.addResult(super.brf(null, deletion, i));
        }

        return superRevision.buildResult();
    }

    public void setBB(BeliefBase beliefBase) {
        if (this.epistemicDistribution != null && !(beliefBase instanceof ChainedEpistemicBB))
            beliefBase = new ChainedEpistemicBB(beliefBase, this, this.epistemicDistribution);

        super.setBB(beliefBase);
    }


    /**
     * Unifies all possible values to an ungrounded epistemic formula
     * (i.e. an epistemic formula with an ungrounded root literal)
     *
     * @param epistemicFormula The epistemic formula with an ungrounded root literal.
     * @return The set of all grounded literals that unify with epistemicFormula.
     */
    public Set<EpistemicFormula> getCandidateFormulas(@NotNull EpistemicFormula epistemicFormula) {
        Set<EpistemicFormula> groundFormulaSet = new HashSet<>();


        // If the root literal is already ground, return a set containing the ground formula.
        if (epistemicFormula.getRootLiteral().getCleanedLiteral().isGround()) {
            groundFormulaSet.add(epistemicFormula);
            return groundFormulaSet;
        }

        // Obtain all possible proposition values from the managed literals object and attempt to unify each value with the ungrounded epistemic formula.
        // All formulas that can be successfully ground will be added to the set.

        // TODO: This has issues. finding by predicate indicator does not incorporate negation in the way that we'd like (i.e. ignore it)
        for (NormalizedWrappedLiteral managedLiteral : epistemicDistribution.getManagedBeliefs(epistemicFormula.getRootLiteral().getNormalizedIndicator())) {
            // Create a cloned/normalized & ungrounded root literal to unify with
            var ungroundedLiteral = epistemicFormula.getRootLiteral().getNormalizedWrappedLiteral();
//            var managedLiteral = managedValue.getNormalizedWrappedLiteral();

            // Attempt to unify with the various managed propositions
            Unifier unifier = ungroundedLiteral.unifyWrappedLiterals(managedLiteral);

            if (unifier != null) {
                var unifiedFormula = epistemicFormula.capply(unifier);

                if (!unifiedFormula.getCleanedOriginal().isGround()) {
                    getLogger().warning("formula " + epistemicFormula + " is still not ground after unifying: " + unifiedFormula);
                    continue;
                }

                groundFormulaSet.add(unifiedFormula);
            }
        }

        return groundFormulaSet;
    }

    /**
     * Gets candidate beliefs using a single world.
     *
     * @return the beliefs iterator, or null if no epistemic dist.
     */
    public Iterator<Literal> getCandidateBeliefs(World world, Literal l, Unifier u) {
        if (this.getEpistemicDistribution() == null) {
            logger.warning("No epistemic distribution set");
            return null;
        }

        // Check to see if the literal is defined in the epistemic distribution
        // If not, evaluate the literal like a normal belief
        if (!this.getEpistemicDistribution().getManagedWorlds().getManagedLiterals().isManagedBelief(new WrappedLiteral(l))) {
            return this.getBB().getCandidateBeliefs(l, u);
        }

        WrappedLiteral wrappedLiteral = new WrappedLiteral(l);
        boolean isPositive = !wrappedLiteral.getCleanedLiteral().negated();

        List<Literal> candidates = new ArrayList<>();

        // Easy case: direct evaluation if ground.
        if (wrappedLiteral.isGround()) {
            if (world.evaluate(wrappedLiteral.getCleanedLiteral()))
                candidates.add(wrappedLiteral.getCleanedLiteral());
            return candidates.iterator();
        }

        // Unground Cases:
        //  1. Positive: location(3, Y) -> should return all in w that unify location(3, Y)
        //  2. Negative: ~location(3, Y)
        //      -> get all in range that unify location(3, Y)
        //      -> Evaluate ~location(3, Y) in world, for all range groundings
        //      -> Should only return iterator if all evaluations are FALSE
        //      -> E.g., We shouldn't return {~location(3, 1), ~location(3, 2), ...} when the world holds location(3, 0)


        // Find all range values that (may) unify with the predicate
        Set<NormalizedWrappedLiteral> managedBeliefs = this.getEpistemicDistribution().getManagedWorlds().getManagedLiterals().getManagedBeliefs(wrappedLiteral.getNormalizedIndicator());

        // Remove excess literals before unifying (positives only)
        if (isPositive) {
            managedBeliefs.removeIf(bel ->
                    !world.evaluate(bel.getCleanedLiteral()));
        }

        // Remove literals that can't unify
        managedBeliefs.removeIf(groundPosLit -> {
            // Get pos literal
            NormalizedWrappedLiteral posLit = wrappedLiteral.getNormalizedWrappedLiteral();

            return !posLit.canUnify(groundPosLit);
        });


        //
        for (var groundPosLit : managedBeliefs) {
            // Check that the world holds the lit if it is positive, and false otherwise.
            if (world.evaluate(groundPosLit.getCleanedLiteral()) == isPositive) {
                // Add to set of candidates (and negate if necessary)
                candidates.add(groundPosLit.getCleanedLiteral().setNegated(isPositive ? Literal.LPos : Literal.LNeg));
            }
        }

        if (isPositive || candidates.size() == managedBeliefs.size())
            return candidates.iterator();
        else
            return null;

    }


    protected SyntaxDistributionBuilder getDistributionBuilder() {
        return this.distributionBuilder;
    }

    @Override
    public void eventAdded(Event e) {
        logger.info("Event added:" + e.getTrigger().toString());
    }

}
