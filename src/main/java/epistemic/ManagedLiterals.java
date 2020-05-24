package epistemic;

import epistemic.wrappers.Proposition;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.PredicateIndicator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class keeps track of a {@link ManagedWorlds} literals (world keys, enumeration values, and a mapping for string proposition to literal object. (E.g. When receiving a proposition from the epistemic.reasoner).
 * This class maintains various sets and mappings of literals to allow for quick access to proposition data.
 */
public class ManagedLiterals {

    // The key set contains the grouping of enumerations. (Typically the head literal of the rule that introduced the enumeration)
    // For example, the key hand("Alice", _) could map to the enumeration values of hand("Alice", "AA"), hand("Alice", "EE"), etc.
    // Todo: is this really needed though?
    private final Set<WrappedLiteral> worldKeysSet;
    private final Map<String, Proposition> propositionStringMap;
    private final Map<WrappedLiteral, Proposition> valueToPropositionMap;
    private final Map<PredicateIndicator, Set<Proposition>> predicateIndicatorPropositionMap;

    public ManagedLiterals() {
        this.worldKeysSet = new HashSet<>();
        this.propositionStringMap = new HashMap<>();
        this.valueToPropositionMap = new HashMap<>();
        this.predicateIndicatorPropositionMap = new HashMap<>();
    }

    @Override
    public ManagedLiterals clone() {
        var clonedLiterals = new ManagedLiterals();
        clonedLiterals.worldKeysSet.addAll(this.worldKeysSet);
        clonedLiterals.propositionStringMap.putAll(this.propositionStringMap);
        clonedLiterals.valueToPropositionMap.putAll(this.valueToPropositionMap);
        clonedLiterals.predicateIndicatorPropositionMap.putAll(this.predicateIndicatorPropositionMap);
        return clonedLiterals;
    }

    /**
     * Called when a world has been added to the managedworlds object. This adds the keys and wrapped values
     * to the sets of managed keys and values.
     *
     * @param world the world that was added
     */
    public void worldAdded(World world) {
        worldKeysSet.addAll(world.keySet());

        for (Proposition val : world.valueSet()) {
            var wrappedPropStr = val.getValue().toSafePropName();
            var existingValue = propositionStringMap.getOrDefault(wrappedPropStr, null);

            if (existingValue != null && !existingValue.getValue().equals(val.getValue()))
                throw new RuntimeException("Existing enumeration maps to the same safe prop name. Prop name should be unique. New Value: " + val + ", Existing value: " + existingValue);

            // Place the new wrapped enumeration value in the mapping.
            propositionStringMap.put(wrappedPropStr, val);
            valueToPropositionMap.put(val.getValue(), val);

            // Map the value predicate indicator to a set of all possible values for that indicator
            var managedPredicateIndicator = getManagedPredicateIndicator(val.getValue().getPredicateIndicator());

            predicateIndicatorPropositionMap.compute(managedPredicateIndicator, (key, cur) -> {
               if(cur == null)
                   cur = new HashSet<>();

               cur.add(val);

               return cur;
            });

        }


    }

    /**
     * Gets the LiteralKey that is mapped to the propName string.
     *
     * @param propName
     * @return LiteralKey object mapped to propName, or null if propName does not map to any enumeration.
     */
    public Proposition getPropositionLiteral(String propName) {
        return this.propositionStringMap.getOrDefault(propName, null);
    }

    /**
     * @param belief The belief to look for in the managed literals set.
     * @return The corresponding Proposition object, or null if the belief is not managed by this object.
     */
    public Proposition getManagedBelief(WrappedLiteral belief)
    {
        return this.valueToPropositionMap.getOrDefault(belief, null);
    }

    public boolean isManagedBelief(PredicateIndicator predicateIndicator)
    {
        return predicateIndicatorPropositionMap.containsKey(predicateIndicator);
    }

    /**
     * Removes negation from predicate indicators.
     * @param predicateIndicator
     * @return
     */
    private PredicateIndicator getManagedPredicateIndicator(PredicateIndicator predicateIndicator)
    {
        if(predicateIndicator == null)
            return null;

        var curFunctor = predicateIndicator.getFunctor();

        if(curFunctor.startsWith("~"))
            curFunctor = curFunctor.substring(1);

        return new PredicateIndicator(predicateIndicator.getNS(), curFunctor, predicateIndicator.getArity());
    }

    public Set<Proposition> getManagedBeliefs(PredicateIndicator predicateIndicator)
    {
        return predicateIndicatorPropositionMap.getOrDefault(getManagedPredicateIndicator(predicateIndicator), new HashSet<>());
    }

    /**
     * Determines if a belief is one that is managed by this object.
     *
     * @param belief The belief to check.
     * @return True if any possible values (in any of the added worlds) match the belief.
     */
    public boolean isManagedBelief(WrappedLiteral belief) {
        return valueToPropositionMap.containsKey(belief);
    }
}
