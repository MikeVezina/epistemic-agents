package epistemic;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import wrappers.Proposition;
import wrappers.WrappedLiteral;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
public class World extends HashMap<WrappedLiteral, Proposition> {

    protected Map<String, Set<World>> accessibleWorlds;
    private Set<WrappedLiteral> cachedWrappedValues;

    public World() {
        this.accessibleWorlds = new HashMap<>();
        this.cachedWrappedValues = new HashSet<>();
    }

    protected World(World world) {
        super(world);
        this.accessibleWorlds = new HashMap<>();
    }

    public void putLiteral(WrappedLiteral key, Literal value)
    {
        this.putProposition(new Proposition(key, value));
    }

    public void putProposition(Proposition proposition) {

        var key = proposition.getKey();
        var value = proposition.getValue();

        if (!value.getLiteral().isGround())
            throw new RuntimeException("Unified literal is not ground");

        if(!doesUnify(key, value))
            throw new RuntimeException("The unifiedLiteral does not unify the key. Failed to put literal into world.");

        this.put(key, proposition);

        // invalidate caches
        this.cachedWrappedValues = null;
    }

    private boolean doesUnify(WrappedLiteral wrappedLiteral, WrappedLiteral val)
    {
        var unifier = new Unifier();
        return unifier.unifies(wrappedLiteral.getLiteral(), val.getLiteral());
    }

    public World clone() {
        return new World(this);
    }


    public Literal toLiteral() {
        Literal literal = ASSyntax.createLiteral("world");
        for (var term : this.values()) {
            literal.addTerm(term.getValue().getLiteral());
        }

        return literal;
    }

    public Set<WrappedLiteral> valueSet() {
        if(cachedWrappedValues != null && !cachedWrappedValues.isEmpty() && cachedWrappedValues.size() == this.values().size())
            return cachedWrappedValues;

        Set<WrappedLiteral> wrappedValues = new HashSet<>();

        for (var v : values()) {
            wrappedValues.add(v.getValue());
        }

        cachedWrappedValues = wrappedValues;

        return wrappedValues;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstVal = true;

        stringBuilder.append("{");

        for (var prop : values()) {
            if (!firstVal)
                stringBuilder.append(", ");
            else
                firstVal = false;

            // Don't show annotations when printing.
            stringBuilder.append(prop.getValueLiteral().clearAnnots());
        }


        stringBuilder
                .append("}")
                .append(", Accessible: ");

        if (accessibleWorlds.isEmpty())
            stringBuilder.append("No accessibility.");
        else if (accessibleWorlds.size() == 1)
        {
            var val = accessibleWorlds.values().iterator().next();
            stringBuilder.append(val.size());
        }

        return stringBuilder.toString();
    }

    /**
     * Evaluate the belief in the current world
     *
     * @param belief The belief to evaluate.
     * @return True if the belief exists (and is true) in the world, and False otherwise.
     */
    public boolean evaluate(Literal belief) {
        if(belief == null)
            return false;

        WrappedLiteral key = new WrappedLiteral(belief);
        return valueSet().contains(key);
    }

    /**
     * Checks to see if the structure of two literals are equivalent.
     * This method will ignore any namespaces, literal negation, and annotations, to directly compare the literals.
     * TODO: Further analysis on whether or not we need to ignore literal negation.
     *
     * @param litOne First literal
     * @param litTwo Second literal
     * @return True if litOne and litTwo have equivalent functors, and terms.
     */
    public boolean propositionEquals(Literal litOne, Literal litTwo) {
        // Check to see if the two literals are equal (ignore NS, negation, and annots).
        litOne = (LiteralImpl) litOne.cloneNS(Literal.DefaultNS);
        litTwo = (LiteralImpl) litTwo.cloneNS(Literal.DefaultNS);
        return litOne.equalsAsStructure(litTwo);
    }

    /**
     * Determines if the belief is a proposition in the current world through successful unification.
     * This ignores the namespace and annotations when checking for unification.
     *
     * @param belief The belief
     * @return True if the belief can unify any key.
     */
    private WrappedLiteral findUnificationKey(Literal belief) {
        Unifier u = new Unifier();

        for (WrappedLiteral extendedLiteral : keySet()) {

            // Set default namespace and remove annotations
            Literal lit = (Literal) extendedLiteral.getLiteral().cloneNS(Literal.DefaultNS);
            lit = lit.clearAnnots();

            Literal bel = (Literal) belief.cloneNS(Literal.DefaultNS);
            bel = bel.clearAnnots();


            if (u.unifies(lit, bel))
                return extendedLiteral;
        }

        return null;
    }

    public void createAccessibility(String agent, Map<WrappedLiteral, Set<World>> binnedWorlds) {
        // Get the intersection of the binned worlds and the current keys.
        var cloneSet = new HashSet<>(this.valueSet());
        cloneSet.retainAll(binnedWorlds.keySet());
        System.out.println(cloneSet);

        accessibleWorlds.clear();

        for (WrappedLiteral key : cloneSet) {
            // Clone the binned worlds
            var worlds = new HashSet<>(binnedWorlds.get(key));

            accessibleWorlds.compute(agent, (a, val) -> {
                if (val == null || val.isEmpty())
                    return worlds;
                else
                    val.retainAll(worlds);

                return val;
            });
        }

    }

    public String getUniqueName()
    {
        return String.valueOf(this.hashCode());
    }


    public Map<String, Set<World>> getAccessibleWorlds() {
        return Map.copyOf(accessibleWorlds);
    }
}
