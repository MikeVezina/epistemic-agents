package epistemic.distribution.generator;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.formula.EpistemicModality;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public abstract class WorldGenerator {
    private final Rule ruleToProcess;
    private final NormalizedWrappedLiteral propKey;
    private final EpistemicAgent epistemicAgent;
    private final Set<NormalizedWrappedLiteral> worldLiteralMatchers;
    private List<Unifier> currentWorldUnifications;
    private final Logger logger = Logger.getLogger("World Generation - " + getClass().getName());

    /**
     * Creates a world generator
     *
     * TODO: Could probably change matcher set to be PredicateIndicators
     *
     * @param agent
     * @param rule
     * @param worldLiteralMatchers
     */
    protected WorldGenerator(EpistemicAgent agent, NormalizedWrappedLiteral propKey, Rule rule, Set<NormalizedWrappedLiteral> worldLiteralMatchers) {
        this.ruleToProcess = rule;
        this.epistemicAgent = agent;
        this.worldLiteralMatchers = worldLiteralMatchers;
        this.propKey = propKey;
    }

    protected NormalizedWrappedLiteral getPropKey() {
        return propKey;
    }

    public static WorldGenerator createGenerator(EpistemicAgent agent, NormalizedWrappedLiteral propKey, @NotNull Rule rule, Set<NormalizedWrappedLiteral> allManagedLiterals) {
        if (rule.getHead().getFunctor().equals(EpistemicModality.POSSIBLE.getFunctor()))
            return new PossiblyGenerator(agent, propKey,rule, allManagedLiterals);

        return new NecessaryGenerator(agent, propKey, rule, allManagedLiterals);
    }

    protected boolean acceptsWorld(@NotNull World world) {
        // Get rule body unifiers...
        var iter = this.ruleToProcess.getBody().logicalConsequence(getWorldLogicalConsequence(world), new Unifier());

        // Set the current world's iterator (cache it for processing)
        setWorldIterator(iter);

        // The processor accepts the world if there are valid unifications
        return this.currentWorldUnifications != null && !this.currentWorldUnifications.isEmpty();
    }

    private void setWorldIterator(Iterator<Unifier> iter) {
        // Set field to null if iterator is null
        if (iter == null) {
            this.currentWorldUnifications = null;
            return;
        }

        this.currentWorldUnifications = new ArrayList<>();
        iter.forEachRemaining(this.currentWorldUnifications::add);
    }

    /**
     * Performs transformations on the world object, returning a list of all
     * worlds to add to our model.
     *
     * @param world The world to transform.
     *              This object does not get added to the model, so you must add it to the returned list if it should be added.
     * @return The list of worlds to add to our model (does not include the world parameter)
     */
    protected abstract Set<World> transformWorld(World world, List<Literal> literalValues);

    protected Set<World> processWorld(World world) {
        // Transform worlds based on rule unifications
        var literals = expandRule();
        return transformWorld(world, literals);
    }


    /**
     * Expands a rule (with variables) into a list of grounded literals. This essentially provides an enumeration of all variables values.
     * This maintains the functor and arity of the rule head, replacing variables with a value.
     * <p>
     * For example, given the beliefs: [test("abc"), test("123")],
     * the rule original_rule(Test) :- test(Test) will be expanded to the following grounded literals:
     * [original_rule("abc"), original_rule("123")]
     *
     * @return A List of ground literals.
     */
    protected List<Literal> expandRule() {
        // Obtain the head and body of the rule
        Literal ruleHead = this.ruleToProcess.getHead();

        // Set up a list of expanded literals
        List<Literal> expandedLiterals = new ArrayList<>();

        if (ruleHead.isGround()) {
            expandedLiterals.add(ruleHead);
            return expandedLiterals;
        }

        // Unify each valid unification with the plan head and add it to the belief base.
        for (Unifier unif : currentWorldUnifications) {
            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) ruleHead.capply(unif);

            // All unified/expanded rules should be ground.
            if (!expandedRule.isGround()) {
                System.out.println("The expanded rule (" + expandedRule + ") is not ground.");
                for (int i = 0; i < expandedRule.getArity(); i++) {
                    Term t = expandedRule.getTerm(i);
                    if (!t.isGround())
                        System.out.println("Term " + t + " is not ground.");
                }
            }

            expandedLiterals.add(expandedRule);
        }

        logger.info("Expanded Rule " + ruleHead.toString() + " -> " + expandedLiterals);
        return expandedLiterals;
    }

    public ManagedWorlds processManagedWorlds(ManagedWorlds worlds) {
        ManagedWorlds extendedWorlds = new ManagedWorlds(worlds.getAgent());

        for (World world : worlds) {
            // If the processor does not accept the world, just forward it to the returned object
            if (!this.acceptsWorld(world)) {
                extendedWorlds.add(world);
                continue;
            }

            Set<World> processedWorlds = this.processWorld(world.createCopy());
            extendedWorlds.addAll(processedWorlds);
        }

        return extendedWorlds;
    }

    private CallbackLogicalConsequence getWorldLogicalConsequence(World world) {
        return new CallbackLogicalConsequence(epistemicAgent, (l, u) -> getCandidateBeliefs(world, l, u));

    }

    // Todo: could probably replace canUnify with predicate indicators?
    public Iterator<Literal> getCandidateBeliefs(World world, Literal l, Unifier u) {
        boolean isWorldLiteral = false;

        for (var wrapped : worldLiteralMatchers) {
            if (wrapped.canUnify(new NormalizedWrappedLiteral(l))) {
                isWorldLiteral = true;
                break;
            }
        }

        // Use the BB to get beliefs for the literal if it is not corresponding to a world
        if (!isWorldLiteral)
            return epistemicAgent.getBB().getCandidateBeliefs(l, u);

        List<Literal> litList = new ArrayList<>();

        if (l.isGround()) {
            if (!world.evaluate(l))
                return null;

            // If ground, just add the literal to list of unifications

            litList.add(l);
            return litList.listIterator();
        }

        // if not ground, we have to unify it with a value
        // TODO: Need to rethink this... how are negations handled about negations?
        for (var val : world.keySet()) {
            if (val.canUnify(new WrappedLiteral(l)))
                litList.add(val.getOriginalLiteral());
        }

        return litList.listIterator();
    }

    protected boolean isNegatedRule() {
        return ruleToProcess.getHead().negated();
    }
}
