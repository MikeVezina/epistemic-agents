package epistemic;

import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * The World class is a map of initial literals to possible enumerations.
 * <p>
 * Random note: What-if indistinguishable worlds mapped to the same hashCode? This would need to be dynamic.
 * <p>
 * The key should be Hashed based on the number of variables (?), as it needs to hash based on the following examples:
 * 1. { alice(_), bob(_) } -> { alice(_), bob(_) }
 * 2. { hand("Alice", _), hand("Bob", _)  } -> { hand("Alice", _), hand("Bob", _)  }
 * 3. { alice("AA"), alice("BB") } -> { alice(_) }
 * <p>
 * <p>
 * { alice(Hand), alice("AA") } -> { alice\1 }
 * { hand(Player, Hand), hand("Alice", Alice), hand("Bob", Bob) } -> hand\2 isn't sufficient ("Alice" and "Bob" should be considered separate).
 */
public class World extends HashMap<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> {
    private final UUID worldId;
    private final Set<NormalizedWrappedLiteral> valuation;
    private final TreeSet<String> sortedPropNames;
    private int hashCache;
    private String sortedPropStr;

    public World() {
        this.worldId = UUID.randomUUID();
        valuation = new HashSet<>();
        sortedPropNames = new TreeSet<>();
        updateHashCache();
    }

    /**
     * Clones everything except the world ID.
     *
     * @param world
     */
    private World(World world) {
        this();

        // We need to clone all Literals
        this.putAll(world);
        this.valuation.addAll(world.valuation);
        this.sortedPropNames.addAll(world.sortedPropNames);
        this.hashCache = world.hashCache;
        sortedPropStr = world.sortedPropStr;
    }

    /**
     * Put key/value in without updating hash. This is useful for putAll without having to invoke obtaining a new hash code
     * @param key
     * @param value
     */
    private void directPut(@NotNull NormalizedWrappedLiteral key, @NotNull NormalizedWrappedLiteral value)
    {
        putIfAbsent(key, new HashSet<>());
        get(key).add(value);

        valuation.add(value);
        sortedPropNames.add(value.toSafePropName());
    }

    public Set<NormalizedWrappedLiteral> put(@NotNull NormalizedWrappedLiteral key, @NotNull NormalizedWrappedLiteral value) {
        directPut(key, value);
        updateHashCache();
        return null;
    }

    private void updateHashCache() {
        // Update hash on new value
        hashCache = sortedPropNames.toString().hashCode();
        sortedPropStr = sortedPropNames.toString();
    }

    /**
     * Appends cloned values in the set to the existing value for key. ADD TESTS to ensure this does not remove existing props!!!
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public Set<NormalizedWrappedLiteral> put(NormalizedWrappedLiteral key, Set<NormalizedWrappedLiteral> value) {
        // Put cloned values into new set (created by put overload)
        value.forEach(val -> this.directPut(key, val));
        updateHashCache();
        return null;
    }

    @Deprecated
    public Set<NormalizedWrappedLiteral> put(NormalizedWrappedLiteral key, Literal value) {
        return put(key, new NormalizedWrappedLiteral(value));
    }

    @Override
    public void putAll(Map<? extends NormalizedWrappedLiteral, ? extends Set<NormalizedWrappedLiteral>> m) {
        for (var entry : m.entrySet())
            this.put(entry.getKey(), entry.getValue());
    }



    /**
     * Creates a copy of the current world, except that the new object contains a different world ID
     * (used for world generation).
     *
     * @return The cloned world with a different random world ID.
     */
    public World createCopy() {
        return new World(this);
    }

    /**
     * Gets a unique randomly generated int for the current world.
     *
     * @return
     */
    public String getWorldId() {
        return this.worldId.toString();
    }

    public Literal toLiteral() {
        Literal literal = ASSyntax.createLiteral("world");
        for (var term : valuation) {
            literal.addTerm(term.getCleanedLiteral());
        }

        return literal;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstVal = true;

        stringBuilder.append("{");

        for (var litValue : sortedPropNames) {
            if (!firstVal)
                stringBuilder.append(", ");
            else
                firstVal = false;

            // Don't show annotations when printing.
            stringBuilder.append(litValue);
        }

        stringBuilder
                .append("}");

        return stringBuilder.toString();
    }

    /**
     * Evaluate the belief in the current world
     *
     * @param belief The belief to evaluate.
     * @return True if a positive belief is a proposition in this world, or if a negative belief is not in this world. False otherwise.
     */
    public boolean evaluate(Literal belief) {
        if (belief == null)
            return false;

        WrappedLiteral key = new WrappedLiteral(belief);

        // The valuation should only contain the belief if the belief is positive (not negated)
        return valuation.contains(key.getNormalizedWrappedLiteral()) != belief.negated();
    }


    public String getUniqueName() {
        return String.valueOf(getWorldId());
    }


    @Override
    public int hashCode() {
        return hashCache;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof World))
            return false;

        var otherWorld = (World) obj;

        // Use hashCode (cached) to filter out worlds that are not equal
        // This significantly reduces the amount of time needed for world generation.
        if (otherWorld.hashCode() != hashCode())
            return false;

        return otherWorld.sortedPropStr.equals(this.sortedPropStr);
    }

    public Set<NormalizedWrappedLiteral> getValuation() {
        return valuation;
    }

    public void removePropositions(NormalizedWrappedLiteral propKey, Set<NormalizedWrappedLiteral> wrappedLiterals) {
        if (!this.containsKey(propKey))
            return;

        var propSet = this.get(propKey);
        propSet.removeAll(wrappedLiterals);

        if (propSet.isEmpty())
            super.remove(propKey);

        this.valuation.removeAll(wrappedLiterals);

        for (var val : wrappedLiterals)
            this.sortedPropNames.remove(val.toSafePropName());

        // Update hash on new value
        updateHashCache();
    }
}
