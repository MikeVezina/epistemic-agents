package epistemic;

import epistemic.wrappers.Proposition;
import jason.EpistemicAgent;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;
import epistemic.wrappers.WrappedLiteral;

import java.util.*;
import java.util.stream.Collector;

/**
 * The ManagedWorlds class is a set of all possible world objects. This class also maintains a ManagedLiterals object, which caches
 * various literal sets and mappings for quick access to what literals are managed by this ManagedWorlds object.
 */
public class ManagedWorlds extends HashSet<World> {

    public static final String PROPS_PROPERTY = "props";
    private ManagedLiterals managedLiterals;
    private final EpistemicAgent epistemicAgent;

    public ManagedWorlds(@NotNull EpistemicAgent epistemicAgent) {
        this.epistemicAgent = epistemicAgent;
        this.managedLiterals = new ManagedLiterals();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this(worlds.epistemicAgent);
        this.addAll(worlds);
        this.managedLiterals = worlds.managedLiterals.clone();
    }


    @Override
    public boolean add(World world) {
        managedLiterals.worldAdded(world);
        return super.add(world);
    }



    /**
     * Checks if the provided belief is a possible value in the set of managed worlds.
     *
     * @param belief The belief LiteralKey to check.
     * @return True if the belief is a managed literal
     */
    public boolean isManagedBelief(WrappedLiteral belief) {
        return managedLiterals.isManagedBelief(belief);
    }

    /**
     * Checks if the provided belief is a possible value in the set of managed worlds. Wraps the literal in a LiteralKey object and then calls the overloaded method that accepts a LiteralKey.
     *
     * @param belief The belief LiteralKey to check.
     * @return True if the belief is a managed literal
     * @see ManagedWorlds#isManagedBelief(WrappedLiteral)
     */
    public boolean isManagedBelief(Literal belief) {
        return this.isManagedBelief(new WrappedLiteral(belief));
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

    public Proposition getManagedProposition(Literal belief) {
        if(belief == null)
            return null;

        return managedLiterals.getManagedBelief(new WrappedLiteral(belief));
    }

    public Proposition getLiteral(String newKnowledge) {
        return managedLiterals.getPropositionLiteral(newKnowledge);
    }
}
