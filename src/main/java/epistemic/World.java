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

    public World() {
        this.worldId = UUID.randomUUID();
        valuation = new HashSet<>();
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
    }

    public Set<NormalizedWrappedLiteral> put(@NotNull NormalizedWrappedLiteral key, @NotNull NormalizedWrappedLiteral value) {
        if (!this.containsKey(key))
            super.put(key, new HashSet<>());

        valuation.add(value);
        super.get(key.getNormalizedWrappedLiteral()).add(value);
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
     * Appends cloned values in the set to the existing value for key.
     * @param key
     * @param value
     * @return
     */
    @Override
    public Set<NormalizedWrappedLiteral> put(NormalizedWrappedLiteral key, Set<NormalizedWrappedLiteral> value) {
        // Put cloned values into new set (created by put overload)
        value.forEach(val -> this.put(key, val.copy()));
        return null;
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
    public int getWorldId() {
        return this.worldId.hashCode();
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

        for (var litValue : valuation) {
            if (!firstVal)
                stringBuilder.append(", ");
            else
                firstVal = false;

            // Don't show annotations when printing.
            stringBuilder.append(litValue.toSafePropName());
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
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof World))
            return false;

        var otherWorld = (World) obj;
        return super.equals(otherWorld);
    }

    public Set<NormalizedWrappedLiteral> getValuation() {
        return new HashSet<>(valuation);
    }
}
