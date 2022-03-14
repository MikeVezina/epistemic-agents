package epistemic.distribution.generator;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NecessaryGenerator extends WorldGenerator {

    public NecessaryGenerator(EpistemicAgent agent, NormalizedWrappedLiteral propKey, Rule rule, Set<NormalizedWrappedLiteral> worldLiteralMatchers) {
        super(agent, propKey, rule, worldLiteralMatchers);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.createCopy();


        // Create normalized wrapped literals (i.e. proposition literals)
        Set<NormalizedWrappedLiteral> wrappedLiterals = literalValues.stream().map(NormalizedWrappedLiteral::new).collect(Collectors.toSet());

        // Handle positive knowledge/necessary by adding all knowledge values
        if (!getRuleFormula().isPropositionNegated()) {
            // This put operation should add all values (not overwrite!)
            transformed.put(getPropKey(), wrappedLiterals);
        } else {
            // If the rule says we know something is not true, the world should remove those values that are not true.
            transformed.removePropositions(getPropKey(), wrappedLiterals);
        }

        // Maintain the world as long as the key still exists (all worlds need to have one value in each prop range to exist)
        if(transformed.containsKey(getPropKey()))
            transformedWorlds.add(transformed);

        return transformedWorlds;
    }

}
