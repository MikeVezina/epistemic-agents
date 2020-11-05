package epistemic.distribution.processor;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.CallbackLogicalConsequence;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.*;


// We need this class to evaluate rule bodies on the worlds..
// Ground literals as filters aren't enough (i.e. for 'not literal')
public class EvaluatorWorld extends WorldProcessorChain {

    private EpistemicAgent agent;
    private Rule rule;
    private Set<WrappedLiteral> worldLiteralMatchers;

    protected EvaluatorWorld(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers) {
        super(agent, rule, worldLiteralMatchers);
    }

    @Override
    protected Set<World> transformWorld(World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        return null;
    }
//
//    public void addChildProcessor(WorldProcessorChain processorChain) {
//        this.childProcessors.add(processorChain);
//    }
//
//    protected Set<World> callChildProcessors(Set<World> transformed)
//    {
//        // Return worlds if they do not need to be processed further
//        if(childProcessors.isEmpty())
//            return transformed;
//
//        Set<World> allWorlds = new HashSet<>();
//
//        for (World processedWorld : transformed) {
//            for (var nextProcessor : childProcessors) {
//                if (nextProcessor.acceptsWorld(processedWorld))
//                    allWorlds.addAll(nextProcessor.processWorld(processedWorld));
//            }
//        }
//
//        return allWorlds;
//    }


}
