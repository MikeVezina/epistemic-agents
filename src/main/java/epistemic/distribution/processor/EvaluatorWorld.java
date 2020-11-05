package epistemic.distribution.processor;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EvaluatorWorld extends WorldProcessorChain{
    public EvaluatorWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals) {
        super(keyLiteral, worldLiterals);
    }

    public EvaluatorWorld(EpistemicAgent epistemicAgent, Rule rule) {
        super(epistemicAgent, rule);
    }

    @Override
    protected List<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        return null;
    }
}
