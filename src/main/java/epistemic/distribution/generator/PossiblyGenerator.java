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

// Turns possibly rules into worlds
public class PossiblyGenerator extends WorldGenerator {

    public PossiblyGenerator(EpistemicAgent agent, NormalizedWrappedLiteral propKey, Rule rule, Set<NormalizedWrappedLiteral> worldLiteralMatchers) {
        super(agent, propKey, rule, worldLiteralMatchers);
    }

    @Override
    protected boolean acceptsWorld(@NotNull World world) {
        return super.acceptsWorld(world);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world,List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.createCopy();

        // Create normalized wrapped literals (i.e. proposition literals)
        Set<NormalizedWrappedLiteral> wrappedLiterals = literalValues.stream().map(NormalizedWrappedLiteral::new).collect(Collectors.toSet());

        var ruleFormula = getRuleFormula();

        // Handle ~possible(...) or possible(~...) or ~possible(~...).
        // TODO:  ~possible(~...) needs to be implemented still
        if (ruleFormula.isModalityNegated() || ruleFormula.isPropositionNegated()) {
            // If the rule says we know something is not true, the world should remove those values that are not true.
            transformed.removePropositions(getPropKey(), wrappedLiterals);
        } else if (!ruleFormula.isPropositionNegated()){
            // This put operation should add all values (not overwrite!)
            transformed.put(getPropKey(), wrappedLiterals);
            for (Literal lit : literalValues) {
                transformed = world.createCopy();
                transformed.put(getPropKey(), new NormalizedWrappedLiteral(lit));
                transformedWorlds.add(transformed);
            }
        } else {
            for (Literal lit : literalValues) {
                transformed = world.createCopy();
                transformed.put(getPropKey(), new NormalizedWrappedLiteral(lit));
                transformedWorlds.add(transformed);
            }
        }

        // Maintain the world as long as the key still exists (all worlds need to have one value in each prop range to exist)
        if(transformed.containsKey(getPropKey()))
            transformedWorlds.add(transformed);

        // -----

        return transformedWorlds;
    }
}
