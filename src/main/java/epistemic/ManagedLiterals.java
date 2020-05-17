package epistemic;

import wrappers.Proposition;
import wrappers.WrappedLiteral;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class keeps track of a {@link ManagedWorlds} literals (world keys, enumeration values, and a mapping for string proposition to literal object. (E.g. When receiving a proposition from the reasoner).
 */
public class ManagedLiterals {

    // The key set contains the grouping of enumerations. (Typically the head literal of the rule that introduced the enumeration)
    // For example, the key hand("Alice", _) could map to the enumeration values of hand("Alice", "AA"), hand("Alice", "EE"), etc.
    // Todo: is this really needed though?
    private final Set<WrappedLiteral> worldKeysSet;

    private final Map<String, WrappedLiteral> propositionMapping;

    private final Map<WrappedLiteral, WrappedLiteral> wrappedValueToKeyMap;

    public ManagedLiterals() {
        this.worldKeysSet = new HashSet<>();
        this.propositionMapping = new HashMap<>();
        this.wrappedValueToKeyMap = new HashMap<>();
    }

    @Override
    public ManagedLiterals clone() {
        var clonedLiterals = new ManagedLiterals();
        clonedLiterals.worldKeysSet.addAll(this.worldKeysSet);
        clonedLiterals.propositionMapping.putAll(this.propositionMapping);
        clonedLiterals.wrappedValueToKeyMap.putAll(this.wrappedValueToKeyMap);
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

        for(Proposition val : world.values())
        {
            var wrappedPropStr = val.getValue().toSafePropName();
            var existingValue = propositionMapping.getOrDefault(wrappedPropStr, null);

            if(existingValue != null && !existingValue.equals(val.getValue()))
                throw new RuntimeException("Existing enumeration maps to the same safe prop name. Prop name should be unique. New Value: " + val + ", Existing value: "+ existingValue);

            // Place the new wrapped enumeration value in the mapping.
            propositionMapping.put(wrappedPropStr, val.getValue());
            wrappedValueToKeyMap.put(val.getValue(), val.getKey());

        }


    }

    /**
     * Gets the LiteralKey that is mapped to the propName string.
     * @param propName
     * @return LiteralKey object mapped to propName, or null if propName does not map to any enumeration.
     */
    public WrappedLiteral getPropositionLiteral(String propName)
    {
        return this.propositionMapping.getOrDefault(propName, null);
    }

    public boolean isManagedBelief(WrappedLiteral belief) {
        return wrappedValueToKeyMap.containsKey(belief);
    }
}
