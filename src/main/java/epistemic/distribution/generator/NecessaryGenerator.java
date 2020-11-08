package epistemic.distribution.generator;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NecessaryGenerator extends WorldGenerator {

    public NecessaryGenerator(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers) {
        super(agent, rule, worldLiteralMatchers);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.clone();

        // Need to handle multiple values per world...
        Set<NormalizedWrappedLiteral> wrappedLiterals = literalValues.stream().map(NormalizedWrappedLiteral::new).collect(Collectors.toSet());
        transformed.addAll(wrappedLiterals);

        transformedWorlds.add(transformed);

        return transformedWorlds;
    }

}
