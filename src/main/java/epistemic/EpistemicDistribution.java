package epistemic;

import epistemic.reasoner.ReasonerSDK;
import epistemic.agent.EpistemicAgent;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSyntax.*;
import epistemic.wrappers.WrappedLiteral;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for being an interface between the Jason objects (TS, Agent, BeliefBase, etc.) and the managed worlds.
 */
public class EpistemicDistribution {

    private final Map<WrappedLiteral, Set<WrappedLiteral>> currentPropValues;
    private final Map<EpistemicFormula, Boolean> currentFormulaEvaluations;
    private final AtomicBoolean needsUpdate;
    private final ManagedWorlds managedWorlds;
    private final ReasonerSDK reasonerSDK;
    private final EpistemicAgent epistemicAgent;

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
    public void agentLoaded()
    {
        // Create the managed worlds
        reasonerSDK.createModel(managedWorlds);
    }

    /**
     * Gets called by the EpistemicAgent during the belief update phase. We use this phase of the reasoning cycle to
     * send proposition updates to the server and generate any relevant knowledge events.
     *
     * @param epistemicFormulas The formulas that will be evaluated.
     *                          Events will be created for the relevant formula evaluations.
     */
    public void buf(Collection<Literal> percepts, Collection<EpistemicFormula> epistemicFormulas) {

        // Pass percepts through this.BRF
        for(Literal literal : percepts)
            brf(literal, null);

        // No need to update props
        if (!this.needsUpdate.get())
            return;

        // Ground all epistemic formulas before evaluating
        Set<EpistemicFormula> groundedFormulas = new HashSet<>();
        for (EpistemicFormula epistemicFormula : epistemicFormulas) {
            groundedFormulas.addAll(epistemicAgent.getCandidateFormulas(epistemicFormula));
        }

        Set<WrappedLiteral> propositionValues = new HashSet<>();
        for(var value : this.currentPropValues.values())
        {
            propositionValues.addAll(value);
        }

        var knowledgeEntries = this.reasonerSDK.updateProps(propositionValues, groundedFormulas).entrySet();

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

    public void brf(Literal beliefToAdd, Literal beliefToDel) {
        addManagedBelief(beliefToAdd);

        Proposition delProp = managedWorlds.getManagedProposition(beliefToDel);

        if (delProp != null) {
            WrappedLiteral delWrapped = new WrappedLiteral(beliefToDel);

            if (isExistingProp(delProp.getValue(), delWrapped)) {
                this.currentPropValues.put(delProp.getValue(), null);
            }
        }
    }

    /**
     * Creates events for the relevant formula plans (+knows(knows(hello)))
     * @param newKnowledge The new knowledge formula
     */
    protected void createKnowledgeEvent(Trigger.TEOperator operator, EpistemicFormula newKnowledge) {
        Trigger te = new Trigger(operator, Trigger.TEType.belief, newKnowledge.getOriginalLiteral());
        epistemicAgent.getTS().updateEvents(new Event(te, Intention.EmptyInt));
    }

    /**
     * Adds a new belief to the map of current proposition values.
     * We also need to do additional consistency checks to make sure there are no contradicting proposition values.
     * @param beliefToAdd
     */
    void addManagedBelief(Literal beliefToAdd)
    {
        if(beliefToAdd == null)
            return;

        Proposition addProp = managedWorlds.getManagedProposition(beliefToAdd);

        if (addProp == null)
            return;

        WrappedLiteral addWrapped = new WrappedLiteral(beliefToAdd);

        if (!isExistingProp(addProp.getValue(), addWrapped)) {
            // We need to maintain BB consistency
            // The following propositions can co-exist for the same prop key:
            // For prop key hand("Alice", Card), we can have the sets:
            // { hand("Alice", "AA") }
            // { hand("Alice", "A8") }
            // { hand("Alice", "88") }
            // { ~hand("Alice", "AA"), ~hand("Alice", "88") }
            // but not:
            // { ~hand("Alice", "AA"), ~hand("Alice", "A8"), ~hand("Alice", "88")  } (at least one of these must be true)
            // { hand("Alice", "AA"), ~hand("Alice", "A8"), ~hand("Alice", "88")  } (the negated propositions are redundant since they are implied from the true proposition)

            this.currentPropValues.compute(addProp.getValue(), (key, val) -> {
                if(val == null)
                    val = new HashSet<>();

                val.add(addWrapped);
                return val;
            });

            this.needsUpdate.set(true);
        }
    }
    public ManagedWorlds getManagedWorlds() {
        return this.managedWorlds;
    }

    public Map<EpistemicFormula, Boolean> evaluateFormulas(Set<EpistemicFormula> epistemicFormula) {
        return this.reasonerSDK.evaluateFormulas(epistemicFormula);
    }


    /**
     * Determines if the key already maps to the newValue.
     *
     * @param key      The key to check the existing value for
     * @param newValue The new value of the key
     * @return True if the new value is equivalent to the old value, false otherwise.
     */
    private boolean isExistingProp(WrappedLiteral key, WrappedLiteral newValue) {
        if (key == null || !this.currentPropValues.containsKey(key))
            return false;

        var curPropSet = this.currentPropValues.get(key);
        return curPropSet.contains(newValue);


    }

    public EpistemicAgent getEpistemicAgent() {
        return this.epistemicAgent;
    }
}
