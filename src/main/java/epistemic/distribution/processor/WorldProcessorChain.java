package epistemic.distribution.processor;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class WorldProcessorChain {
    // A list of sub-processors
    private final LinkedList<WorldProcessorChain> childProcessors;
    private final List<Literal> worldLiterals;
    private final List<Literal> filterLiterals;
    private final WrappedLiteral keyWrappedLiteral;

    protected WorldProcessorChain(WrappedLiteral keyLiteral, List<Literal> worldLiterals) {
        this(keyLiteral, worldLiterals, new ArrayList<>());
    }

    protected WorldProcessorChain(WrappedLiteral keyLiteral, List<Literal> unifiedLiterals, List<Literal> filterLiterals) {
        this.childProcessors = new LinkedList<>();
        this.keyWrappedLiteral = keyLiteral;
        this.worldLiterals = unifiedLiterals;
        this.filterLiterals = filterLiterals;
    }

    public void addChildProcessor(WorldProcessorChain processorChain) {
        this.childProcessors.add(processorChain);
    }

    protected boolean acceptsWorld(@NotNull World world) {
        for (var filterLit : filterLiterals)
            if (!world.evaluate(filterLit))
                return false;
        return true;
    }

    /**
     * Performs transformations on the world object, returning a list of all
     * worlds to add to our model.
     *
     * @param world The world to transform.
     *              This object does not get added to the model, so you must add it to the returned list if it should be added.
     * @return The list of worlds to add to our model (does not include the world parameter)
     */
    protected abstract List<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues);

    protected List<World> processWorld(World world) {

        List<World> transformed = transformWorld(world, keyWrappedLiteral, worldLiterals);

        // Return worlds if they do not need to be processed further
        if(childProcessors.isEmpty())
            return transformed;

        List<World> allWorlds = new ArrayList<>();

        for (World processedWorld : transformed) {
            for (var nextProcessor : childProcessors) {
                if (nextProcessor.acceptsWorld(processedWorld))
                    allWorlds.addAll(nextProcessor.processWorld(processedWorld));
            }
        }

        return allWorlds;
    }

    public WrappedLiteral getKeyWrappedLiteral() {
        return keyWrappedLiteral;
    }

    public List<Literal> getWorldLiterals() {
        return worldLiterals;
    }

    public List<Literal> getFilterLiterals() {
        return filterLiterals;
    }

    public ManagedWorlds createManagedWorlds(EpistemicAgent epistemicAgent) {
        ManagedWorlds managedWorlds = new ManagedWorlds(epistemicAgent);

        // Process worlds starting with a null world
        managedWorlds.addAll(processWorld(new World()));

        return managedWorlds;
    }
}
