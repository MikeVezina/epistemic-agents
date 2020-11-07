package epistemic;

import com.google.gson.annotations.Expose;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedPredicateIndicator;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

/**
 * The ManagedWorlds class is a set of all possible world objects. This class also maintains a ManagedLiterals object, which caches
 * various literal sets and mappings for quick access to what literals are managed by this ManagedWorlds object.
 */
public class ManagedWorlds extends HashSet<World> {

    private ManagedLiterals managedLiterals;

    @Expose(serialize = false, deserialize = false)
    private final EpistemicAgent epistemicAgent;

    public ManagedWorlds(@NotNull EpistemicAgent epistemicAgent) {
        this.epistemicAgent = epistemicAgent;
        this.managedLiterals = new ManagedLiterals();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this(worlds.epistemicAgent);
        this.addAll(worlds);
        this.managedLiterals = worlds.managedLiterals.copy();
    }


    @Override
    public boolean add(World world) {
        managedLiterals.worldAdded(world);
        return super.add(world);
    }

    public ManagedLiterals getManagedLiterals() {
        return managedLiterals;
    }

    /**
     * Generates a set of possible proposition sets that we can use when updating the reasoner model propositions.
     * The propositions in each inner set element can be ORd with eachother, otherwise we use AND.
     * Example:
     * {
     *     {location(0,0), location(1,1)}
     *     {percept(right, block)}
     * }
     *
     * will generate: (location(0,0) OR location(1,1)) AND (percept(right, block))
     *
     *
     * @param currentPropValues
     * @return
     */
    public Set<Set<WrappedLiteral>> generatePropositionSets(Map<NormalizedPredicateIndicator, Set<WrappedLiteral>> currentPropValues) {
        Set<Set<WrappedLiteral>> propositionSet = new HashSet<>();

        if(currentPropValues.isEmpty())
            return propositionSet;

        for (var overloadedLiterals : currentPropValues.values())
        {
            // This is where we check if proposition values should be AND or OR.
            // if two literals share a common world, they can be 'AND' (because they are two separate facts)
            // if two literals do NOT share a common world, they should be 'OR'. (because they can no coexist)

            Map<WrappedLiteral, Set<WrappedLiteral>> commonLiterals = new HashMap<>();

            for(var literal : overloadedLiterals)
            {
                Set<World> worlds = managedLiterals.getRelevantWorlds(literal);

                if(commonLiterals.isEmpty())
                {
                    // Add self to hashset
                    commonLiterals.put(literal, new HashSet<>());
                    commonLiterals.get(literal).add(literal);
                    continue;
                }

                // Ensure negated literals are always on their own
                if(literal.getCleanedLiteral().negated())
                {
                    commonLiterals.put(literal, new HashSet<>());
                    commonLiterals.get(literal).add(literal);
                    continue;
                }

                boolean foundCommonWorlds = false;

                // Find common worlds
                for(var litKey : commonLiterals.keySet())
                {
                    // Don't add to negated literal sets
                    if(litKey.getCleanedLiteral().negated())
                        continue;

                    var relevantWorlds = managedLiterals.getRelevantWorlds(litKey);
                    var res = worlds.stream().anyMatch(relevantWorlds::contains);

                    if(!res) {
                        commonLiterals.get(litKey).add(literal);
                        foundCommonWorlds = true;
                        break;
                    }
                }

                // If there are no common worlds between two belief literals, then we can OR these propositions
                if(!foundCommonWorlds)
                {
                    if(!commonLiterals.containsKey(literal))
                        commonLiterals.put(literal,new HashSet<>());
                    commonLiterals.get(literal).add(literal);
                }


            }

            propositionSet.addAll(commonLiterals.values());
        }

        return propositionSet;
    }

    /**
     * @return a clone of the current managed worlds object. Copies over any current propositions.
     * This will only add the contained worlds to the cloned object, this will not clone any of the contained worlds.
     */
    public ManagedWorlds clone() {
        return new ManagedWorlds(this);
    }

    /**
     * @return A collector that can be used to create a ManagedWorld object from collected worlds.
     */
    public static Collector<World, ManagedWorlds, ManagedWorlds> WorldCollector(EpistemicAgent epistemicAgent) {
        return Collector.of(
                () -> new ManagedWorlds(epistemicAgent),
                ManagedWorlds::add, (result1, result2) -> {
                    result1.addAll(result2);
                    return result1;
                });
    }

    public Set<World> getRelevantWorlds(Literal belief) {
        if (belief == null)
            return null;

        return managedLiterals.getRelevantWorlds(new WrappedLiteral(belief));
    }

    public WrappedLiteral getManagedWrappedLiteral(Literal belief) {
        if (belief == null)
            return null;

        WrappedLiteral wrappedLiteral = new WrappedLiteral(belief);

        if (managedLiterals.isManagedBelief(wrappedLiteral))
            return wrappedLiteral;

        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Generated Worlds (Size: ");
        builder.append(this.size());
        builder.append("): \r\n");
        for (World world : this) {
            builder.append("    ");
            builder.append(world.toLiteral());
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public EpistemicAgent getAgent() {
        return this.epistemicAgent;
    }
}
