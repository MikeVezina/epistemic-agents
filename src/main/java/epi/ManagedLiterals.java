package epi;

import wrappers.LiteralKey;

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
    private final Set<LiteralKey> worldKeysSet;
    // A Set of all Literal values that are managed (values of all worlds)
    private final Set<LiteralKey> managedValues;

    private final Map<String, LiteralKey> propositionMapping;

    public ManagedLiterals() {
        this.worldKeysSet = new HashSet<>();
        this.managedValues = new HashSet<>();
        this.propositionMapping = new HashMap<>();
    }

    @Override
    public ManagedLiterals clone() {
        var clonedLiterals = new ManagedLiterals();
        clonedLiterals.managedValues.addAll(this.managedValues);
        clonedLiterals.worldKeysSet.addAll(this.worldKeysSet);
        clonedLiterals.propositionMapping.putAll(this.propositionMapping);
        return clonedLiterals;
    }

    public void addWorld(World world) {
        worldKeysSet.addAll(world.keySet());

        var wrappedValues = world.wrappedValues();
        for(var val : wrappedValues)
        {
            var wrappedPropStr = val.toSafePropName();
            var existingValue = propositionMapping.getOrDefault(wrappedPropStr, null);

            if(existingValue != null && !existingValue.equals(val))
                throw new RuntimeException("Existing enumeration maps to the same safe prop name. Prop name should be unique. New Value: " + val + ", Existing value: "+ existingValue);

            // Place the new wrapped enumeration value in the mapping.
            propositionMapping.put(wrappedPropStr, val);

        }

        managedValues.addAll(wrappedValues);
    }

    /**
     * Gets the LiteralKey that is mapped to the propName string.
     * @param propName
     * @return LiteralKey object mapped to propName, or null if propName does not map to any enumeration.
     */
    public LiteralKey getPropositionLiteral(String propName)
    {
        return this.propositionMapping.getOrDefault(propName, null);
    }

    public boolean isManagedBelief(LiteralKey belief) {
        return managedValues.contains(belief);
    }
}
