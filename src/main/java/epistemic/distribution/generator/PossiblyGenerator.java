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

// Turns possibly rules into worlds
public class PossiblyGenerator extends WorldGenerator {

    public PossiblyGenerator(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers) {
        super(agent, rule, worldLiteralMatchers);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        for (Literal lit : literalValues) {
            World transformed = world.clone();
            transformed.putProposition(new NormalizedWrappedLiteral(lit));
            transformedWorlds.add(transformed);
        }

        return transformedWorlds;
    }
}
