package epistemic.distribution.processor;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NecessaryWorld extends WorldProcessorChain {

    public NecessaryWorld(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers) {
        super(agent, rule, worldLiteralMatchers);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.clone();

        // Need to handle multiple values per world...
        for (Literal lit : literalValues)
            transformed.putLiteral(new WrappedLiteral(lit), lit);

        transformedWorlds.add(transformed);

        return transformedWorlds;
    }
}
