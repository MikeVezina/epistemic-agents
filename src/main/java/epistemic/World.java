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
public class World extends HashSet<NormalizedWrappedLiteral> {
    private final UUID worldId;

    public World() {
        this.worldId = UUID.randomUUID();
    }

    /**
     * Clones everything except the world ID.
     * @param world
     */
    private World(World world)
    {
        this();
        this.addAll(world);
    }

    @Deprecated
    protected void putLiteral(@NotNull WrappedLiteral key, @NotNull Literal value) {
        add(new NormalizedWrappedLiteral(value));
    }

    @Override
    public boolean add(NormalizedWrappedLiteral normalizedWrappedLiteral) {
        // New proposition should be ground!
        if (!normalizedWrappedLiteral.isGround())
            throw new RuntimeException("Attempted to add non-ground proposition to world: " + normalizedWrappedLiteral);

        return super.add(normalizedWrappedLiteral);
    }

    /**
     * Creates a copy of the current world, except that the new object contains a different world ID
     * (used for world generation).
     * @return The cloned world with a different random world ID.
     */
    public World createCopy() {
        return new World(this);
    }

    /**
     * Gets a unique randomly generated int for the current world.
     * @return
     */
    public int getWorldId()
    {
        return this.worldId.hashCode();
    }

    public Literal toLiteral() {
        Literal literal = ASSyntax.createLiteral("world");
        for (var term : this) {
            literal.addTerm(term.getCleanedLiteral());
        }

        return literal;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstVal = true;

        stringBuilder.append("{");

        for (var litValue : this) {
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
     * @return True if the belief exists (and is true) in the world, and False otherwise.
     */
    public boolean evaluate(Literal belief) {
        if (belief == null)
            return false;

        WrappedLiteral key = new WrappedLiteral(belief);
        return this.contains(key);
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

}
