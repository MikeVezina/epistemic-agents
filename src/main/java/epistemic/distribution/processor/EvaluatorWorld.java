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
    public EvaluatorWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals) {
        super(keyLiteral, worldLiterals);
    }


    private EpistemicAgent agent;
    private Rule rule;
    private Set<WrappedLiteral> worldLiteralMatchers;
    /**
     *
     * @param agent The agent (For other literal logical consequences)
     * @param rule The rule to evaluate on each world
     * @param worldLiteralMatchers The literal matchers that should be evaluated on the worlds
     */
    public EvaluatorWorld(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers)
    {
        super(null, null);
        this.agent = agent;
        this.rule = rule;
        this.worldLiteralMatchers = worldLiteralMatchers;
    }
    @Override
    protected Set<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {

        // Get rule body unifiers...
        var iter = this.rule.getBody().logicalConsequence(getWorldLogicalConsequence(world), new Unifier());

        var arr = new HashSet<World>();
        arr.add(world);

        // iter is null or empty means that this world is not applicable for child processors
        // Return the world un-transformed
        if(iter == null || !iter.hasNext())
             return arr;

        // Pass world to child processors if the rule is applicable to the world
        return super.callChildProcessors(arr);
    }

    @Override
    protected Set<World> processWorld(World world) {
        // Override process world so that child processors do not get called
        return transformWorld(world, getKeyWrappedLiteral(), getWorldLiterals());
    }

    private CallbackLogicalConsequence getWorldLogicalConsequence(World world)
    {
        return new CallbackLogicalConsequence(agent, (l, u) -> getCandidateBeliefs(world,l, u));

    }

    public Iterator<Literal> getCandidateBeliefs(World world, Literal l, Unifier u) {
        boolean isWorldLiteral = false;

        for(var wrapped : worldLiteralMatchers)
        {
            if(wrapped.canUnify(new WrappedLiteral(l)))
            {
                isWorldLiteral = true;
                break;
            }
        }

        // Use the BB to get beliefs for the literal if it is not corresponding to a world
        if(!isWorldLiteral)
            return agent.getBB().getCandidateBeliefs(l, u);

        if(!world.evaluate(l))
            return null;

        List<Literal> litList = new ArrayList<>();
        litList.add(l);
        return litList.listIterator();
    }
}
