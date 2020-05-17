package epistemic;

import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;
import wrappers.WrappedLiteral;

import java.util.*;
import java.util.stream.Collector;

public class ManagedWorlds extends HashSet<World> {

    public static final String PROPS_PROPERTY = "props";

    private ManagedLiterals managedLiterals;

    private final Map<WrappedLiteral, WrappedLiteral> currentPropValues;

    private final EpistemicAgent epistemicAgent;

    public ManagedWorlds(@NotNull EpistemicAgent epistemicAgent) {
        this.epistemicAgent = epistemicAgent;
        this.managedLiterals = new ManagedLiterals();
        this.currentPropValues = new HashMap<>();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this(worlds.epistemicAgent);
        this.addAll(worlds);
        this.managedLiterals = worlds.managedLiterals.clone();

        // Copy over props
        this.currentPropValues.putAll(worlds.currentPropValues);
    }


    @Override
    public boolean add(World world) {
        managedLiterals.worldAdded(world);
        return super.add(world);
    }

    /**
     * Handles when a belief managed by this object has been added to the belief base.
     *
     * @param belief The managed belief
     */
    private void addBelief(WrappedLiteral belief) {
       // this.props.add(belief);
    }

    private void removeBelief(WrappedLiteral belief) {
        //this.props.remove(belief);
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

    /**
     * Todo: add checks for BB consistency.
     * Todo: This needs more work. This adds beliefs but does not remove any if a prop is false.
     * @param newKnowledge
     */
    public void addKnowledge(Set<String> newKnowledge) {
        List<Literal> knowledge = new ArrayList<>();
        for (String prop : newKnowledge) {
            var literal = this.managedLiterals.getPropositionLiteral(prop);
            if (literal != null)
                knowledge.add((Literal) literal.getLiteral().cloneNS(Atom.DefaultNS));
        }
    }
}
