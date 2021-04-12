package epistemic;

import epistemic.wrappers.NormalizedPredicateIndicator;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class keeps track of a {@link ManagedWorlds} literals (world keys, enumeration values, and a mapping for string proposition to literal object. (E.g. When receiving a proposition from the epistemic.reasoner).
 * This class maintains various sets and mappings of literals to allow for quick access to proposition data.
 */
public class ManagedLiterals {

    private final Map<String, NormalizedWrappedLiteral> safePropStringMap;
    private final Map<NormalizedWrappedLiteral, Set<World>> valueToWorldMap;
    private final Map<NormalizedPredicateIndicator, Set<WrappedLiteral>> predicateIndicatorPropositionMap;

    public ManagedLiterals() {
        this.safePropStringMap = new HashMap<>();
        this.valueToWorldMap = new HashMap<>();
        this.predicateIndicatorPropositionMap = new HashMap<>();
    }

    public ManagedLiterals copy() {
        var clonedLiterals = new ManagedLiterals();
        clonedLiterals.safePropStringMap.putAll(this.safePropStringMap);
        clonedLiterals.valueToWorldMap.putAll(this.valueToWorldMap);
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
        for (NormalizedWrappedLiteral val : world.getValuation())
            addProposition(val, world);
    }

    private void addProposition(NormalizedWrappedLiteral valueLiteral, World world) {

        // If we've never seen this value before...
        if (!safePropStringMap.containsKey(valueLiteral.toSafePropName())) {
//        if (!valueToWorldMap.containsKey(valueLiteral)) {
            var wrappedPropStr = valueLiteral.toSafePropName();
            var existingValue = safePropStringMap.getOrDefault(wrappedPropStr, null);

            if (existingValue != null && !existingValue.equals(valueLiteral))
                throw new RuntimeException("Existing enumeration maps to the same safe prop name. Prop name should be unique. New Value: " + valueLiteral + ", Existing value: " + existingValue);

            // Place the new wrapped enumeration value in the mapping.
            safePropStringMap.put(wrappedPropStr, valueLiteral);
            valueToWorldMap.put(valueLiteral, new HashSet<>());
        }

        // Add world to existing worlds
        valueToWorldMap.get(valueLiteral).add(world);

        // Map the value predicate indicator to a set of all possible values for that indicator
        var normalizedIndicator = valueLiteral.getNormalizedIndicator();

        if (!predicateIndicatorPropositionMap.containsKey(normalizedIndicator))
            predicateIndicatorPropositionMap.put(normalizedIndicator, new HashSet<>());

        predicateIndicatorPropositionMap.get(normalizedIndicator).add(valueLiteral);

    }

    /**
     * @param belief The belief to look for in the managed literals set.
     * @return The corresponding Proposition object, or null if the belief is not managed by this object.
     */
    public Set<World> getRelevantWorlds(WrappedLiteral belief) {
        return this.valueToWorldMap.getOrDefault(belief.getNormalizedWrappedLiteral(), new HashSet<>());
    }

    public boolean isManagedBelief(NormalizedPredicateIndicator predicateIndicator) {
        return predicateIndicatorPropositionMap.containsKey(predicateIndicator);
    }

    /**
     * Gets any Propositions that match the predicate indicator. Negated functors will be ignored.
     * i.e. ~hand/1 will be adjusted to hand/1 since a proposition negation is denoted by absence of the proposition.
     *
     * @param predicateIndicator The managed belief predicate indicator
     * @return A set of all managed beliefs that match the normalized predicate indicator, or an empty set if none exist.
     */
    public Set<WrappedLiteral> getManagedBeliefs(NormalizedPredicateIndicator predicateIndicator) {
        return predicateIndicatorPropositionMap.getOrDefault(predicateIndicator, new HashSet<>());
    }

    /**
     * Determines if a belief is one that is managed by this object.
     *
     * @param belief The belief to check.
     * @return True if any possible values (in any of the added worlds) match the belief.
     */
    public boolean isManagedBelief(WrappedLiteral belief) {
        return predicateIndicatorPropositionMap.containsKey(belief.getNormalizedIndicator());
    }

    public boolean isManagedBelief(Literal belief) {
        if (belief == null)
            return false;

        return this.isManagedBelief(new WrappedLiteral(belief));
    }

    public int size() {
        return this.safePropStringMap.size();
    }
}
