package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.agent.EpistemicAgent;
import epistemic.agent.RevisionResult;
import epistemic.formula.EpistemicFormula;
import epistemic.reasoner.ReasonerSDK;
import epistemic.wrappers.NormalizedPredicateIndicator;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Event;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class is responsible for being an interface between the Jason objects (TS, Agent, BeliefBase, etc.) and the managed worlds.
 */
public class EpistemicDistribution {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Map<NormalizedPredicateIndicator, Set<WrappedLiteral>> currentPropValues;
    private final Map<EpistemicFormula, Boolean> currentFormulaEvaluations;
    private final AtomicBoolean needsUpdate;
    private ManagedWorlds managedWorlds;
    private final ReasonerSDK reasonerSDK;
    private final EpistemicAgent epistemicAgent;
    private final Logger eventLogger = Logger.getLogger(getClass().getName() + " - Events");

    public EpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds) {
        this(agent, managedWorlds, new ReasonerSDK());

    }

    public EpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds, @NotNull ReasonerSDK reasonerSDK) {
        needsUpdate = new AtomicBoolean(false);
        this.currentPropValues = new HashMap<>();
        this.currentFormulaEvaluations = new HashMap<>();
        this.epistemicAgent = agent;
        this.managedWorlds = managedWorlds;

        // Create new reasonerSDK object that listens to the managed world events
        // This sends any necessary API requests
        this.reasonerSDK = reasonerSDK;

    }

    /**
     * Should be called once the agent has been loaded.
     */
    public void agentLoaded() {
        // Create the managed worlds
        if (managedWorlds.isEmpty())
            logger.info("Skipping model creation for empty model");
        else
            reasonerSDK.createModel(managedWorlds);
    }

    public synchronized void setUpdatedWorlds(ManagedWorlds managedWorlds) {
        this.managedWorlds = managedWorlds;
        this.agentLoaded();

        this.needsUpdate.set(true);

        // Update model with no formula evaluations
        updateModel(new ArrayList<>());
    }

    /**
     * Gets called by the EpistemicAgent during the belief update phase. We use this phase of the reasoning cycle to
     * send proposition updates to the server and generate any relevant knowledge events.
     *
     * @param epistemicFormulas The formulas that will be evaluated.
     *                          Events will be created for the relevant formula evaluations.
     */
    public void buf(Collection<Literal> currentPercepts, Collection<Literal> deletions, Collection<EpistemicFormula> epistemicFormulas) {

        // Nothing has changed.
        // Create an empty list of percepts
        if (currentPercepts == null)
            currentPercepts = new ArrayList<>();

        if (deletions == null)
            deletions = new ArrayList<>();

        // Pass deleted percepts through this.BRF
        for (Literal literal : deletions) {
            this.brf(null, literal);
        }

        // Pass percepts through this.BRF
        for (Literal literal : currentPercepts) {
            this.brf(literal, null);
        }

        // Do NOT update if the current intention is adding a belief!
        // This will overload the reasoner if we have a sequence of belief additions.
        // We should wait until the end
        var selected = getEpistemicAgent().getTS().getC().getSelectedIntention();

        if (selected != null && !selected.isFinished()) {
            IntendedMeans top = selected.peek();

            // If the current intention is not complete
            if (!top.isFinished()) {
                // Skip updates if the current intention instruction is to modify belief
                if (isBeliefInstruction(top.getCurrentStep())) {
                    logger.info("Skipping reasoner update until non-belief instruction");
                    return;
                }

            }
        }

        updateModel(epistemicFormulas);
    }

    private boolean isBeliefInstruction(PlanBody currentStep) {
        if (currentStep == null || currentStep.getBodyType() == null)
            return false;

        PlanBody.BodyType stepType = currentStep.getBodyType();

        return stepType.equals(PlanBody.BodyType.addBel)
                || stepType.equals(PlanBody.BodyType.addBelNewFocus)
                || stepType.equals(PlanBody.BodyType.addBelBegin)
                || stepType.equals(PlanBody.BodyType.delBel)
                || stepType.equals(PlanBody.BodyType.delBelNewFocus)
                || stepType.equals(PlanBody.BodyType.delAddBel);
    }

    private synchronized void updateModel(Collection<EpistemicFormula> epistemicFormulas) {
        // No need to update props
        if (!this.shouldUpdateReasoner())
            return;

        logger.info("Reasoner update flag has been raised! Updating reasoner.");

        if (managedWorlds.isEmpty()) {
            logger.info("Skipping model update for empty model");
            return;
        }

        // Ground all epistemic formulas before evaluating
        Set<EpistemicFormula> groundedFormulas = new HashSet<>();
        for (EpistemicFormula epistemicFormula : epistemicFormulas) {
            groundedFormulas.addAll(epistemicAgent.getCandidateFormulas(epistemicFormula));
        }


        var knowledgeEntries = this.reasonerSDK.updateProps(managedWorlds.generatePropositionSets(currentPropValues), groundedFormulas).entrySet();

        for (var knowledgePropEntry : knowledgeEntries) {
            var formula = knowledgePropEntry.getKey();
            var valuation = knowledgePropEntry.getValue();

            // Insert the valuation if the formula does not exist in the map
            var previousValuation = currentFormulaEvaluations.put(formula, valuation);

            // If the valuation is false and we did not have a previous valuation,
            // or if the valuation has not changed since the previous update
            // then we do not create any knowledge updates
            if ((!valuation && previousValuation == null) || valuation.equals(previousValuation))
                continue;

            // The event operator will be 'add' if the formula evaluates to true and was previously a false/null value.
            // The event operator will be 'del' if the formula evaluates to false and was previously true
            var eventOperator = valuation ? Trigger.TEOperator.add : Trigger.TEOperator.del;

            createKnowledgeEvent(eventOperator, formula);
        }

        this.needsUpdate.set(false);
    }

    /**
     * Testing purposes only...
     */
    @Deprecated
    protected Map<WrappedLiteral, Set<WrappedLiteral>> getCurrentPropValues() {
        return new HashMap<>();
    }

    public RevisionResult brf(Literal beliefToAdd, Literal beliefToDel) {
        var revisions = new RevisionResult();

        boolean isManagedAdd = managedWorlds.getManagedLiterals().isManagedBelief(beliefToAdd);
        boolean isManagedDel = managedWorlds.getManagedLiterals().isManagedBelief(beliefToDel);

        // Place the belief in the additions revisions since it isn't managed by us and should be passed directly to the BB
        if (!isManagedAdd && beliefToAdd != null)
            revisions.addAddition(beliefToAdd);

        // Place the belief in the deletions revisions since it isn't managed by us and should be passed directly to the BB
        if (!isManagedDel && beliefToDel != null)
            revisions.addDeletion(beliefToDel);

        // If both add/del are not managed (or they contained null), then we do not need
        // to process the props further.
        if (isManagedAdd) {
            WrappedLiteral addWrapped = new WrappedLiteral(beliefToAdd);
            revisions.addResult(addManagedBelief(addWrapped));
        }

        if (isManagedDel) {
            WrappedLiteral delWrapped = new WrappedLiteral(beliefToDel);

            // Remove wrapped deletion from current prop set for key
            var propSet = this.currentPropValues.get(delWrapped.getNormalizedIndicator());

            // propSet -> may be null if distribution gets rebuilt
            if (propSet != null) {
                propSet.remove(delWrapped);

                if (propSet.isEmpty())
                    this.currentPropValues.remove(delWrapped.getNormalizedIndicator());
            }
            revisions.addDeletion(delWrapped.getOriginalLiteral());

            this.needsUpdate.set(true);
        }


        // If there are no revisions but add/del belief are not null, then there is an issue!
        if (revisions.getDeletions().isEmpty() && revisions.getAdditions().isEmpty() && beliefToAdd != null && beliefToDel != null)
            logger.warning("Belief revision is incorrect. Revision result is invalid! (This is a bug!)");

        return revisions;

    }

    /**
     * Creates events for the relevant formula plans (+knows(knows(hello)))
     *
     * @param newKnowledge The new knowledge formula
     */
    protected void createKnowledgeEvent(Trigger.TEOperator operator, EpistemicFormula newKnowledge) {
        Trigger te = new Trigger(operator, Trigger.TEType.belief, newKnowledge.getCleanedOriginal());
        epistemicAgent.getTS().updateEvents(new Event(te, Intention.EmptyInt));
    }

    /**
     * @return True if the reasoner needs to be updated, false otherwise.
     */
    protected boolean shouldUpdateReasoner() {
        return this.needsUpdate.get();
    }

    /**
     * Adds a new belief to the map of current proposition values.
     * We also need to do additional consistency checks to make sure there are no contradicting proposition values.
     *
     * @param beliefToAdd
     */
    RevisionResult addManagedBelief(@NotNull WrappedLiteral beliefToAdd) {
        var revisions = new RevisionResult();

        if (!currentPropValues.containsKey(beliefToAdd.getNormalizedIndicator()))
            currentPropValues.put(beliefToAdd.getNormalizedIndicator(), new HashSet<>());

        currentPropValues.get(beliefToAdd.getNormalizedIndicator()).add(beliefToAdd);
        revisions.addAddition(beliefToAdd.getOriginalLiteral());

        this.needsUpdate.set(true);

        return revisions;
    }

    public ManagedWorlds getManagedWorlds() {
        return this.managedWorlds;
    }

    public Map<EpistemicFormula, Boolean> evaluateFormulas(Set<EpistemicFormula> epistemicFormula) {
        if (managedWorlds.isEmpty()) {
            logger.info("Skipping formula evaluation for empty model");
            return new HashMap<>();
        }

        return this.reasonerSDK.evaluateFormulas(epistemicFormula);
    }

    public EpistemicAgent getEpistemicAgent() {
        return this.epistemicAgent;
    }

    public Set<WrappedLiteral> getManagedBeliefs(NormalizedPredicateIndicator predicateIndicator) {
        return getManagedWorlds().getManagedLiterals().getManagedBeliefs(predicateIndicator);
    }
}
