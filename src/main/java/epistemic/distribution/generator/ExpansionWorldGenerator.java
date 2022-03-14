package epistemic.distribution.generator;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Clones current worlds for every possible prop value. Some worlds may be filtered by other world generators
 */
public class ExpansionWorldGenerator extends WorldGenerator{

    private final List<Literal> litsToAdd;

    public ExpansionWorldGenerator(EpistemicAgent agent, NormalizedWrappedLiteral propKey, List<Literal> litsToAdd) {
        super(agent, propKey, null, null);
        this.litsToAdd = litsToAdd;
    }

    @Override
    protected boolean acceptsWorld(@NotNull World world) {
        return super.acceptsWorld(world);
    }

    @Override
    protected Set<World> processWorld(World world) {
        return transformWorld(world, litsToAdd);
    }

    @Override
    public ManagedWorlds processManagedWorlds(ManagedWorlds worlds) {

        ManagedWorlds extendedWorlds = new ManagedWorlds(worlds.getAgent());

        worlds.parallelStream().map(world -> this.processWorld(world)).forEach(extendedWorlds::addAll);

        return extendedWorlds;
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world,List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();


        // Necessary when we've already specified knowledge in the world
        if(world.containsKey(getPropKey()))
        {
            transformedWorlds.add(world);
            return transformedWorlds;
        }

        for (Literal lit : literalValues) {
            World transformed = world.createCopy();
            transformed.put(getPropKey(), new NormalizedWrappedLiteral(lit));
            transformedWorlds.add(transformed);
        }

        return transformedWorlds;
    }

    @Override
    public String toString() {
        return "ExpansionWorldGenerator { " + getPropKey().getCleanedLiteral() + "}";
    }
}
