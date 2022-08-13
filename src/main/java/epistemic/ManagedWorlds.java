package epistemic;

import epistemic.agent.EpistemicAgent;
import epistemic.distribution.consequences.WorldConsequences;
import epistemic.distribution.generator.CallbackLogicalConsequence;
import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The ManagedWorlds class is a set of all possible world objects. This class also maintains a ManagedLiterals object, which caches
 * various literal sets and mappings for quick access to what literals are managed by this ManagedWorlds object.
 */
public class ManagedWorlds extends HashSet<World> {

    private ManagedLiterals managedLiterals;
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
    public synchronized boolean add(World world) {
        managedLiterals.worldAdded(world);
        return super.add(world);
    }

    @Override
    public synchronized boolean remove(Object o)
    {
        if(!(o instanceof World))
            return false;

        World world = (World) o;

        this.managedLiterals.worldRemoved(world);
        return super.remove(world);
    }

    public synchronized ManagedLiterals getManagedLiterals() {
        return managedLiterals;
    }

    public synchronized void updateWorld(World oldW, World newW) {
        this.remove(oldW);
        this.add(newW);
    }



    /**
     * @return a clone of the current managed worlds object. Copies over any current propositions.
     * This will only add the contained worlds to the cloned object, this will not clone any of the contained worlds.
     */
    public ManagedWorlds clone() {
        return new ManagedWorlds(this);
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

    public void addRanges(Map<NormalizedWrappedLiteral, List<Literal>> managedRange) {
        for (var values : managedRange.values())
            this.managedLiterals.addRange(values);
    }

    public static CallbackLogicalConsequence CreateWorldLogicalConsequence(EpistemicAgent agent, World world) {
        return new CallbackLogicalConsequence(agent, (l, u) -> agent.getCandidateBeliefs(world, l, u));

    }


    public Map<World, List<WorldConsequences>> logicalConsequences(LogicalFormula l, Unifier preUnifier) {

        Map<World, List<WorldConsequences>> worldConsequences = new HashMap<>();

        // Find worlds that satisfy the rule's logical consequences
        for (World world : this) {
            worldConsequences.putAll(logicalConsequences(world, l, preUnifier));
        }
        return worldConsequences;
    }

    public Map<World, List<WorldConsequences>> logicalConsequences(World world, LogicalFormula l, Unifier preUnifier) {

        Map<World, List<WorldConsequences>> worldConsequences = new HashMap<>();

        // Find worlds that satisfy the rule's logical consequences
        var iterator = l.logicalConsequence(CallbackLogicalConsequence.CreateWorldLogicalConsequence(epistemicAgent, world), preUnifier);

        // logCons doesn't return null when no cons (only empty iterator)
        if (iterator == null || !iterator.hasNext())
            return worldConsequences;

        // Insert empty list
        worldConsequences.putIfAbsent(world, new ArrayList<>());

        while (iterator.hasNext()) {
            Unifier nextUnif = iterator.next();
            LogicalFormula unifiedLit = (LogicalFormula) l.capply(nextUnif);

            // Add to list
            worldConsequences.get(world).add(new WorldConsequences(world, nextUnif, unifiedLit));
        }

        return worldConsequences;
    }

}
