package epistemic.distribution.processor;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.CallbackLogicalConsequence;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class WorldProcessorChain {
    private final Rule ruleToProcess;
    private final EpistemicAgent epistemicAgent;
    private final Set<WrappedLiteral> worldLiteralMatchers;
    private LinkedList<Unifier> currentWorldUnifications;

    protected WorldProcessorChain(EpistemicAgent agent, Rule rule, Set<WrappedLiteral> worldLiteralMatchers) {
        this.ruleToProcess = rule;
        this.epistemicAgent = agent;
        this.worldLiteralMatchers = worldLiteralMatchers;
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
        if(iter == null) {
            this.currentWorldUnifications = null;
            return;
        }

        this.currentWorldUnifications = new LinkedList<>();
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
    protected abstract Set<World> transformWorld(World world, WrappedLiteral literalKey, List<Literal> literalValues);

    protected Set<World> processWorld(World world) {
        // Transform worlds based on rule unifications
        var literals = expandRule();
        return transformWorld(world, new WrappedLiteral(this.ruleToProcess.getHead()), literals);
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
    protected LinkedList<Literal> expandRule() {
        // Obtain the head and body of the rule
        Literal ruleHead = this.ruleToProcess.getHead();

        // Set up a list of expanded literals
        LinkedList<Literal> expandedLiterals = new LinkedList<>();

        if(ruleHead.isGround())
        {
            expandedLiterals.add(ruleHead);
            return expandedLiterals;
        }

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = currentWorldUnifications.listIterator();

        // Unify each valid unification with the plan head and add it to the belief base.
        while (unifIterator.hasNext()) {
            Unifier unif = unifIterator.next();

            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) ruleHead.capply(unif);
            System.out.println("Unifying " + ruleHead.toString() + " with " + unif + ". Result: " + expandedRule);

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

        return expandedLiterals;
    }

    public ManagedWorlds processManagedWorlds(ManagedWorlds worlds)
    {
        ManagedWorlds extendedWorlds = new ManagedWorlds(worlds.getAgent());

        for(World world : worlds)
        {
            // If the processor does not accept the world, just forward it to the returned object
            if(!this.acceptsWorld(world))
            {
                extendedWorlds.add(world);
                continue;
            }

            Set<World> processedWorlds = this.processWorld(world.clone());
            extendedWorlds.addAll(processedWorlds);
        }

        return extendedWorlds;
    }

    public ManagedWorlds createManagedWorlds(EpistemicAgent epistemicAgent) {
        var managedWorlds = new ManagedWorlds(epistemicAgent);

        // add a blank world before processing the managed worlds
        managedWorlds.add(new World());

        return processManagedWorlds(managedWorlds);
    }

    private CallbackLogicalConsequence getWorldLogicalConsequence(World world)
    {
        return new CallbackLogicalConsequence(epistemicAgent, (l, u) -> getCandidateBeliefs(world,l, u));

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
            return epistemicAgent.getBB().getCandidateBeliefs(l, u);

        if(!world.evaluate(l))
            return null;

        List<Literal> litList = new ArrayList<>();
        litList.add(l);
        return litList.listIterator();
    }

}
