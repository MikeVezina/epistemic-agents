package epistemic.distribution.processor;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.propositions.MultiValueProposition;
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
    protected Set<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.clone();

        // Need to handle multiple values per world...
        Set<NormalizedWrappedLiteral> wrappedLiterals = literalValues.stream().map(NormalizedWrappedLiteral::new).collect(Collectors.toSet());
        transformed.putProposition(new MultiValueProposition(literalKey.getNormalizedWrappedLiteral(), wrappedLiterals));

        transformedWorlds.add(transformed);

        return transformedWorlds;
    }

}
