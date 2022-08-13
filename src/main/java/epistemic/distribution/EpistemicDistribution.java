package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.agent.RevisionResult;
import epistemic.distribution.formula.EpistemicFormula;
import epistemic.distribution.formula.EpistemicModality;
import epistemic.distribution.formula.KnowEpistemicFormula;
import epistemic.distribution.formula.PossibleEpistemicFormula;
import epistemic.distribution.ontic.EventModel;
import epistemic.reasoner.ReasonerSDK;
import epistemic.wrappers.NormalizedPredicateIndicator;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Event;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class is responsible for being an interface between the Jason objects (TS, Agent, BeliefBase, etc.) and the managed worlds.
 */
public class EpistemicDistribution {

    private static final String PRE_FUNCTOR = "pre";
    private static final String POST_FUNCTOR = "post";
    private static final String EVENT_FUNCTOR = "event";
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Set<KnowEpistemicFormula> currentKnowledge;
    private final Map<EpistemicFormula, Boolean> currentFormulaEvaluations;
    private final AtomicBoolean needsUpdate;
    private ManagedWorlds managedWorlds;
    private final ReasonerSDK reasonerSDK;
    private final EpistemicAgent epistemicAgent;

    private final Map<Term, EventModel> eventModels;

    private final Logger eventLogger = Logger.getLogger(getClass().getName() + " - Events");

    public EpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds) {
        this(agent, managedWorlds, new ReasonerSDK());
    }

    public EpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds, @NotNull ReasonerSDK reasonerSDK) {
        needsUpdate = new AtomicBoolean(false);
        this.currentKnowledge = new HashSet<>();
        this.currentFormulaEvaluations = new HashMap<>();
        this.epistemicAgent = agent;
        this.managedWorlds = managedWorlds;
        this.eventModels = new HashMap<>();

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

        loadEventRules();
    }

    public synchronized void setUpdatedWorlds(ManagedWorlds managedWorlds) {
        this.managedWorlds = managedWorlds;
        this.agentLoaded();

        this.needsUpdate.set(true);

        // Update model with no formula evaluations
        updateModel(new ArrayList<>());
    }

    private synchronized void loadEventRules() {
        var iter = getEpistemicAgent().getBB().getCandidateBeliefs(new PredicateIndicator(EVENT_FUNCTOR, 1));

        while (iter != null && iter.hasNext()) {
            Literal next = iter.next();
            Rule eventRule;

            // Parse rule from literal
            if (!next.isRule())
                eventRule = new Rule(next, Literal.LTrue);
            else
                eventRule = (Rule) next;

            Term eventIdTerm = eventRule.getTerm(0);

            EventModel eventModel = new EventModel(epistemicAgent, eventRule, eventIdTerm);

            // Find event pre-conditions
            eventModel.setPreRule(findEventPreConditionRule(eventIdTerm));

            // Find event pre-conditions
            eventModel.setPostLiteral(findEventPostCondition(eventIdTerm));

            this.eventModels.put(eventIdTerm, eventModel);
        }

    }

    private Rule findEventPreConditionRule(Term eventIdTerm) {
        var preIter = getEpistemicAgent().getBB().getCandidateBeliefs(new PredicateIndicator(PRE_FUNCTOR, 1));

        while (preIter != null && preIter.hasNext()) {
            Literal lit = preIter.next();

            if(!lit.getTerm(0).equals(eventIdTerm))
                continue;

            if (!lit.isRule()) {
                logger.warning("Non-rule pre-condition: " + lit);
                continue;
            } else if (preIter.hasNext())
                logger.warning("There is more than one pre-condition rule for: " + eventIdTerm + ". Using first rule only.");

            return (Rule) lit;
        }

        return null;
    }

    private Literal findEventPostCondition(Term eventIdTerm) {
        var postIter = getEpistemicAgent().getBB().getCandidateBeliefs(new PredicateIndicator(POST_FUNCTOR, 3));

        while (postIter != null && postIter.hasNext()) {
            Literal lit = postIter.next();

            if(!lit.getTerm(0).equals(eventIdTerm))
                continue;

            if (postIter.hasNext())
                logger.warning("There is more than one post-condition rule for: " + eventIdTerm + ". Using first rule only.");

            return lit;
        }


        // Search for post/2
        postIter = getEpistemicAgent().getBB().getCandidateBeliefs(new PredicateIndicator(POST_FUNCTOR, 3));

        while (postIter != null && postIter.hasNext()) {
            Literal lit = postIter.next();

            if(!lit.getTerm(0).equals(eventIdTerm))
                continue;

            if (postIter.hasNext())
                logger.warning("There is more than one post-condition rule for: " + eventIdTerm + ". Using first rule only.");

            return lit;
        }


        return null;
    }

    /**
     * Gets called by the EpistemicAgent during the belief update phase. We use this phase of the reasoning cycle to
     * send proposition updates to the server and generate any relevant knowledge events.
     *
     * @param epistemicFormulas The formulas that will be evaluated.
     *                          Events will be created for the relevant formula evaluations.
     */
    public void buf(Collection<Literal> currentPercepts, Collection<Literal> deletions, Collection<EpistemicFormula> epistemicFormulas) {

        if (currentPercepts != null) {
            Map<World, World> ruleTransitions = new HashMap<>(); // getTransitionRules();
            if (!ruleTransitions.isEmpty()) {
                if (!reasonerSDK.processTransitions(ruleTransitions))
                    logger.warning("Failed to process transitions. API success was false.");
            }
        }

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

        // Process event models here?
        if(processEventModels())
        {
            reasonerSDK.createModel(managedWorlds);
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
                if (this.shouldUpdateReasoner() && isBeliefInstruction(top.getCurrentStep())) {
                    logger.info("Skipping reasoner update until non-belief instruction");
                    logger.info("Current knowledge: " + currentKnowledge);
                    return;
                }

            }
        }

        updateModel(epistemicFormulas);
    }

    private boolean processEventModels() {
        Map<EventModel, Set<World>> preConditionWorlds = new HashMap<>();

        // Collects all worlds applicable to all events (they can not have coinciding worlds)
        Set<World> eventWorlds = new HashSet<>();

        // Process applicable events and their pre-conditions
        for (var eventModel : eventModels.values()) {
            if (eventModel.isApplicable()) {
                preConditionWorlds.put(eventModel, eventModel.getPreConditionWorlds());

                for(var wSet : preConditionWorlds.values())
                {
                    if(wSet.stream().anyMatch(eventWorlds::contains))
                    {
                        logger.warning("Non-exclusive events!");
                        throw new RuntimeException("Non-exclusive events");
                    }

                    eventWorlds.addAll(wSet);
                }
            }
        }

        // No events to process.
        if(preConditionWorlds.isEmpty())
            return false;

        if(eventWorlds.size() > managedWorlds.size())
        {
            throw new RuntimeException("More worlds than previous model!!");
        }

        // We need to remove worlds so that only those that match pre are maintained
        managedWorlds.removeIf(o -> true);

        for (var eventPost : preConditionWorlds.entrySet())
        {
            var event = eventPost.getKey();
            var preWorlds = eventPost.getValue();
            var postWorlds = new HashSet<World>();


            for(World preWorld : preWorlds)
            {
                var postWorld = event.applyPostConditionWorld(preWorld);
                postWorlds.add(postWorld);
                managedWorlds.add(postWorld);
            }
            System.out.println(postWorlds);
        }

        return true;
    }

    private Map<World, World> getTransitionRules() {
        BeliefBase bb = epistemicAgent.getBB();

        // Get all bels/rules that match: 'possible(_)'
        var iter = bb.getCandidateBeliefs(new PredicateIndicator("possible", 1));

        Map<World, World> worldTransitions = new HashMap<>();

        while (iter != null && iter.hasNext()) {
            var next = iter.next();

            // Rules only
            if (!next.isRule())
                continue;

            Rule possRule = (Rule) next;

            var unif = new Unifier();

            // Get list of epistemic literal references (as positive literals) in the rule body
            var depList = getPossibleRuleLiterals(possRule, unif);
            worldTransitions.putAll(depList);


            //            for (var key : depList.keySet()) {
            //                worldTransitions.put(key, depList.get(key));
            //            }

            System.out.println(worldTransitions);

        }

        for (var entry : worldTransitions.entrySet()) {
            String from = "";
            String to = "";
            for (var prop : entry.getKey().getValuation())
                if (prop.toSafePropName().contains("location"))
                    from = prop.toSafePropName();
            for (var prop : entry.getValue().getValuation())
                if (prop.toSafePropName().contains("location"))
                    to = prop.toSafePropName();

            logger.info(from + " -> " + to);
        }

        return worldTransitions;
    }


    /**
     * Obtains a list of epistemic formula literals in a given rule body.
     * The list will only contain positive literals (no negations).
     *
     * @param r
     * @param unif
     * @return
     */
    private Map<World, World> getPossibleRuleLiterals(Rule r, Unifier unif) {

        // Map each grounding of the rule head to its set of positive (epistemic) grounding body literals
        Map<WrappedLiteral, Set<NormalizedWrappedLiteral>> ruleTransitions = new HashMap<>();

        // World transitions: key = pre-world, val = post-world
        Map<World, World> worldTransition = new HashMap<>();

        // Get Head formula
        WrappedLiteral head = new WrappedLiteral(r.getHead());
        EpistemicFormula headEpistemic = createEpistemicFormula(head.getCleanedLiteral());
        WrappedLiteral rootHead = headEpistemic.getRootLiteral();


        // Get the head literal without the 'possible' wrapping
        WrappedLiteral rootHeadEpistemic = headEpistemic.getRootLiteral();


        var worldLogCons = getManagedWorlds().logicalConsequences(r.getBody(), unif);

        // Find worlds that satisfy the rule's logical consequences
        for (World preWorld : worldLogCons.keySet()) {
            var worldCons = worldLogCons.get(preWorld);

            for (var con : worldCons) {
                Unifier u = con.getUnifier();

                var postWorldCons = managedWorlds.logicalConsequences(new WrappedLiteral(r.getHead()).getCleanedLiteral(), u);


                ///head.getOriginalLiteral().capply(///)

                // Check if the head literal is one that exists in any/all worlds
//                if (!this.getManagedWorlds().getManagedLiterals().isRangeLiteral(rootHead.getNormalizedWrappedLiteral())) {
//                    logger.warning("Transition rule head is not in the range: " + headEpistemic);
//                }

                Set<World> matchingWorlds = this.getManagedWorlds().getManagedLiterals().getRelevantWorlds(rootHeadEpistemic);

                if (matchingWorlds.isEmpty()) {
                    logger.fine("No worlds that match post transition: " + rootHeadEpistemic.getCleanedLiteral());
                    continue;
                }

                // Also, make sure the head only matches with one world:
                if (matchingWorlds.size() != 1) {
                    logger.warning("Transition matches " + matchingWorlds.size() + " worlds?");
                    throw new RuntimeException("Bad Transition: " + matchingWorlds.size() + ", from: " + r);
                }
                World postWorld = matchingWorlds.iterator().next();
                worldTransition.put(preWorld, postWorld);

            }
        }

        return worldTransition;
    }

    @Deprecated
    private void oldTransition() {
        // Old:
        //        var iterator = r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {
        //
        //            // Find those in body that are managed (PI?)
        //            EpistemicFormula formula = createEpistemicFormula(l);
        //
        //            if (formula == null)
        //                return getEpistemicAgent().getBB().getCandidateBeliefs(l, u);
        //
        //            WrappedLiteral epistemicLiteral = formula.getRootLiteral();
        //
        //            // Bind any variables from existing unifier
        //            WrappedLiteral unifEpistemicLit = new WrappedLiteral((Literal) epistemicLiteral.getCleanedLiteral().capply(u));
        //
        //            ungroundEpistemicBodyLiterals.add(unifEpistemicLit.getNormalizedWrappedLiteral());
        //
        //            Set<Literal> matchingRangeValues = new HashSet<>();
        //
        //            // Match with range
        //            for (var rangeLit : managedWorlds.getManagedLiterals().getManagedBeliefs(epistemicLiteral.getNormalizedIndicator())) {
        //                if (rangeLit.canUnify(unifEpistemicLit)) {
        //                    matchingRangeValues.add(rangeLit.getCleanedLiteral());
        ////                    groundEpistemicBodyLiterals.add(rangeLit.getCleanedLiteral());
        //                }
        //            }
        //
        //            // TODO: This may not work for dependent literals because the values are generated by a range(.) rule which won't be picked up by this.
        //            return matchingRangeValues.iterator();
        //        }), unif);
        //
        //        while (iterator.hasNext()) {
        //            Unifier u = iterator.next();
        //            WrappedLiteral head = new WrappedLiteral(r.headCApply(u));
        //
        //            if (!head.isGround()) {
        //                logger.warning("Transition rule head is not ground: " + head + ", from: " + r);
        //                continue;
        //            }
        //
        //            EpistemicFormula headEpistemic = createEpistemicFormula(head.getCleanedLiteral());
        //
        //            if (!this.getManagedWorlds().getManagedLiterals().isRangeLiteral(headEpistemic.getRootLiteral().getNormalizedWrappedLiteral())) {
        //                logger.warning("Transition rule head is not in the range: " + headEpistemic);
        //                continue;
        //            }
        //
        //            // Get the head literal without the 'possible' wrapping
        //            WrappedLiteral rootHeadEpistemic = headEpistemic.getRootLiteral();
        //
        //            // Map the ground rule head to a set of ground epistemic values
        //            Set<NormalizedWrappedLiteral> groundEpistemicBodyLiterals = ruleTransitions.getOrDefault(rootHeadEpistemic, new HashSet<>());
        //            for (var unground : ungroundEpistemicBodyLiterals) {
        //                var unifLit = (Literal) unground.getCleanedLiteral().capply(u);
        //                groundEpistemicBodyLiterals.add(new NormalizedWrappedLiteral(unifLit));
        //            }
        //
        //            ruleTransitions.put(rootHeadEpistemic, groundEpistemicBodyLiterals);
        //
        //        }
        //
        //        return ruleTransitions;
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
        logger.info("Current knowledge: " + currentKnowledge);

        if (managedWorlds.isEmpty()) {
            logger.info("Skipping model update for empty model");
            return;
        }

        // Ground all epistemic formulas before evaluating
        Set<EpistemicFormula> groundedFormulas = new HashSet<>();
        for (EpistemicFormula epistemicFormula : epistemicFormulas) {
            groundedFormulas.addAll(epistemicAgent.getCandidateFormulas(epistemicFormula));
        }

        var knowledgeEntries = this.reasonerSDK.updateProps(currentKnowledge, groundedFormulas).entrySet();

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
     * Converts all formulas to a knowledge formula (i.e. all possible formulas get converted while all
     * knowledge formulas are preserved).
     *
     * @param currentFormulas The set of current epistemic beliefs as formulas.
     * @return
     */
    private Set<KnowEpistemicFormula> getKnowledgeFormulas(Set<EpistemicFormula> currentFormulas) {
        Set<KnowEpistemicFormula> knowledgeSet = new HashSet<>();

        for (EpistemicFormula formula : currentFormulas) {
            knowledgeSet.add(convertToKnowledge(formula));
        }

        return knowledgeSet;
    }

    /**
     * The possible formula will be converted to knowledge.
     * This function only accepts possible formulas that have a negated modality
     * (this is how we store them)
     *
     * @param formula The possible formula being converted to knowledge
     * @return
     * @throws IllegalArgumentException if the formula parameter does not have a negated modality
     */
    private KnowEpistemicFormula convertToKnowledge(@NotNull EpistemicFormula formula) {
        if (formula instanceof KnowEpistemicFormula)
            return (KnowEpistemicFormula) formula;

        if (!formula.isModalityNegated()) {
            logger.warning("Converting non-negated formula: " + formula.toString());
            throw new IllegalArgumentException("Converting non-negated formula: " + formula.toString());
        }

        return new KnowEpistemicFormula(formula.getRootLiteral().getOriginalLiteral().setNegated(formula.isPropositionNegated() ? Literal.LPos : Literal.LNeg));
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

        EpistemicFormula addFormula = createEpistemicFormula(beliefToAdd);
        EpistemicFormula delFormula = createEpistemicFormula(beliefToDel);

        boolean isManagedAdd = addFormula != null;
        boolean isManagedDel = delFormula != null;

        // Place the belief in the additions revisions since it isn't managed by us and should be passed directly to the BB
        if (!isManagedAdd && beliefToAdd != null)
            revisions.addAddition(beliefToAdd);

        // Place the belief in the deletions revisions since it isn't managed by us and should be passed directly to the BB
        if (!isManagedDel && beliefToDel != null)
            revisions.addDeletion(beliefToDel);

        // If both add/del are not managed (or they contained null), then we do not need
        // to process the props further.
        if (isManagedAdd)
            brfAdd(addFormula, revisions);

        if (isManagedDel)
            brfDel(delFormula, revisions);

        // If there are no revisions but add/del belief are not null, then there is an issue!
        if (revisions.getDeletions().isEmpty() && revisions.getAdditions().isEmpty() && beliefToAdd != null && beliefToDel != null)
            logger.warning("Belief revision is incorrect. Revision result is invalid! (This is a bug!)");

        return revisions;

    }

    protected void brfAdd(@NotNull EpistemicFormula added, @NotNull RevisionResult revisions) {
        // Redirect added possibilities (i.e. +possible x) to brf delete (this is actually a removal of an eliminated possibility, i.e. -~possible x)
        if (added.getEpistemicModality().equals(EpistemicModality.POSSIBLE) && !added.isModalityNegated()) {
            // Create new possible formula with negated modality
            PossibleEpistemicFormula possibleFormula = (PossibleEpistemicFormula) added;
            EpistemicFormula newFormula = possibleFormula.deriveNewPossibleFormula(true, possibleFormula.isPropositionNegated());

            // Call brf delete using negated formula
            brfDel(newFormula, revisions);
            return;
        }

        var knowFormula = convertToKnowledge(added);
        currentKnowledge.add(knowFormula);
        this.needsUpdate.set(true);
        revisions.addAddition(added.getOriginalWrappedLiteral().getOriginalLiteral());
    }

    protected void brfDel(@NotNull EpistemicFormula removed, @NotNull RevisionResult revisions) {
        // Redirect removed possibilities (i.e. -possible) to brf add (this is actually the addition of an eliminated possibility, i.e. +~possible)
        if (removed.getEpistemicModality().equals(EpistemicModality.POSSIBLE) && !removed.isModalityNegated()) {
            // Create new possible formula with negated modality
            PossibleEpistemicFormula possibleFormula = (PossibleEpistemicFormula) removed;
            EpistemicFormula newFormula = possibleFormula.deriveNewPossibleFormula(true, possibleFormula.isPropositionNegated());

            // Call brf delete using negated formula
            brfAdd(newFormula, revisions);
            return;
        }
        var knowFormula = convertToKnowledge(removed);
        currentKnowledge.remove(knowFormula);
//        currentKnowledge.remove(convertToKnowledge(removed));
        revisions.addDeletion(removed.getOriginalWrappedLiteral().getOriginalLiteral());

        this.needsUpdate.set(true);
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

    public ManagedWorlds getManagedWorlds() {
        return this.managedWorlds;
    }

    public Map<EpistemicFormula, Boolean> evaluateFormulas(Set<EpistemicFormula> epistemicFormula) {
        if (managedWorlds.isEmpty()) {
            logger.info("Skipping formula evaluation for empty model");
            return new HashMap<>();
        }

        // Update the model if the flag is raised
        updateModel(new ArrayList<>());

        return this.reasonerSDK.evaluateFormulas(epistemicFormula);
    }

    public EpistemicAgent getEpistemicAgent() {
        return this.epistemicAgent;
    }

    /**
     * Creates an epistemic formula object from a literal, asserting that it belongs to the current epistemic model.
     *
     * @param original The literal to convert to an epistemic formula.
     * @return A valid EpistemicFormula object if the root literal (i.e. atomic proposition) of 'original' is part of the current
     * epistemic distribution (it is a managed belief). Otherwise, null.
     */
    public EpistemicFormula createEpistemicFormula(Literal original) {
        if (original == null)
            return null;

        // We want to omit creating a wrapped literal for non-epistemic queries.
        if (!original.getFunctor().equals(EpistemicModality.POSSIBLE.getFunctor())
                && !getManagedWorlds().getManagedLiterals().isManagedBelief(new NormalizedPredicateIndicator(original.getPredicateIndicator()))) {
            logger.info(original.getPredicateIndicator() + " is not an epistemic formula/literal (no matching managed literal indicator)");
            return null;
        }


        var epistemicFormula = EpistemicFormula.fromLiteral(original);

        // Check to see that the root literal is a managed belief (i.e. part of the model)
        if (getManagedWorlds().getManagedLiterals().isManagedBelief(epistemicFormula.getRootLiteral()))
            return epistemicFormula;

        return null;
    }

    public Set<NormalizedWrappedLiteral> getManagedBeliefs(NormalizedPredicateIndicator predicateIndicator) {
        return getManagedWorlds().getManagedLiterals().getManagedBeliefs(predicateIndicator);
    }
}
