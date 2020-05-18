package epistemic;

import epistemic.wrappers.Proposition;
import epistemic.wrappers.WrappedLiteral;

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

    public ManagedLiterals() {
        this.worldKeysSet = new HashSet<>();
        this.propositionStringMap = new HashMap<>();
        this.valueToPropositionMap = new HashMap<>();
    }

    @Override
    public ManagedLiterals clone() {
        var clonedLiterals = new ManagedLiterals();
        clonedLiterals.worldKeysSet.addAll(this.worldKeysSet);
        clonedLiterals.propositionStringMap.putAll(this.propositionStringMap);
        clonedLiterals.valueToPropositionMap.putAll(this.valueToPropositionMap);
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

        for (Proposition val : world.values()) {
            var wrappedPropStr = val.getValue().toSafePropName();
            var existingValue = propositionStringMap.getOrDefault(wrappedPropStr, null);

            if (existingValue != null && !existingValue.getValue().equals(val.getValue()))
                throw new RuntimeException("Existing enumeration maps to the same safe prop name. Prop name should be unique. New Value: " + val + ", Existing value: " + existingValue);

            // Place the new wrapped enumeration value in the mapping.
            propositionStringMap.put(wrappedPropStr, val);
            valueToPropositionMap.put(val.getValue(), val);

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
