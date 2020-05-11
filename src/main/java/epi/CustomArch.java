package epi;

import eis.bb.event.BBEventType;
import eis.bb.KBeliefBase;
import jason.architecture.AgArch;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.DefaultBeliefBase;
import reasoner.WorldRequest;
import wrappers.LiteralKey;

import java.util.*;
import java.util.function.Consumer;

public class CustomArch extends AgArch {

    private static final Atom KB = ASSyntax.createAtom("kb");
    private static final Atom PROP_ANNOT = ASSyntax.createAtom("prop");

    private ManagedWorlds managedWorlds;
    private WorldRequest worldRequest;
    private final Set<Literal> propLiterals;
    private final Set<LiteralKey> dontKnowTerms;

    public CustomArch() {
        propLiterals = new HashSet<>();
        dontKnowTerms = new HashSet<>();

    }

    @Override
    public void init() throws Exception {
        super.init();
        processDistribution();
    }


    public KBeliefBase getKBeliefBase()
    {
        return (KBeliefBase) getTS().getAg().getBB();
    }

    /**
     * Process the distribution of worlds
     */
    protected void processDistribution() {
        // Gets all literals in the kb belief base that are marked with 'prop'

        processLiterals(this::processPropLiterals, this::processDontKnow);

        // Generate the map of literal enumerations
        var literalMap = generateLiteralEnumerations(this.propLiterals);

        // Create the distribution of worlds
        this.managedWorlds = generateWorlds(literalMap);
        this.worldRequest = new WorldRequest(managedWorlds);

        getKBeliefBase().addListener(managedWorlds, BBEventType.REGISTER_ALL);


        System.out.println();
        System.out.println("Generated Worlds:");
        for (World world : managedWorlds) {
            System.out.println(world.toLiteral());
        }


    }

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private void processPropLiterals(Literal literal) {
        if (literal.hasAnnot(PROP_ANNOT))
            this.propLiterals.add(literal);
    }

    /**
     * Processes beliefs that match the "dont_know" functor. Adds all "not known" terms to the set, does nothing otherwise.
     *
     * @param literal The literal / belief
     */
    private void processDontKnow(Literal literal) {
        if (!literal.getFunctor().equals("dont_know"))
            return;

        for (Term t : literal.getTerms()) {
            if (t.isLiteral())
                this.dontKnowTerms.add(new LiteralKey((Literal) t));
        }
    }

    /**
     * Iterates through the belief base (kb namespace only) and calls any processing functions.
     */
    @SafeVarargs
    protected final void processLiterals(Consumer<Literal>... consumers) {
        // Get the agent's belief base
        var beliefBase = this.getTS().getAg().getBB();

        // We need to iterate all beliefs.
        // We can't use beliefBase.getCandidateBeliefs(...) [aka the pattern matching function] because
        // the pattern matching function doesn't allow us to pattern match by just namespace and annotation
        // (it requires a functor and arity)

        // Iterate through the belief base and call the consumers
        for (Literal belief : beliefBase) {
            if (!belief.getNS().equals(KB))
                continue;

            for (Consumer<Literal> literalConsumer : consumers)
                literalConsumer.accept(belief);
        }

    }


    /**
     * Expands a rule (with variables) into a list of grounded literals. This essentially provides an enumeration of all variables values.
     * This maintains the functor and arity of the rule head, replacing variables with a value.
     * <p>
     * For example, given the beliefs: [test("abc"), test("123")],
     * the rule original_rule(Test) :- test(Test) will be expanded to the following grounded literals:
     * [original_rule("abc"), original_rule("123")]
     *
     * @param rule The rule to expand.
     * @return A List of ground literals.
     */
    protected LinkedList<Literal> expandRule(Rule rule) {
        // Obtain the head and body of the rule
        Literal ruleHead = rule.getHead();
        LogicalFormula ruleBody = rule.getBody();

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(this.getTS().getAg(), new Unifier());

        // Set up a list of expanded literals
        LinkedList<Literal> expandedLiterals = new LinkedList<>();

        // Unify each possible unification with the plan head and add it to the belief base.
        while (unifIterator.hasNext()) {
            Unifier unif = unifIterator.next();

            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) ruleHead.capply(unif);
            System.out.println("Unifying " + rule.getFunctor() + " with " + unif + ". Result: " + expandedRule);

            // All unified/expanded rules should be ground.
            if (!expandedRule.isGround())
                System.out.println("The expanded rule (" + expandedRule + ") is not ground.");

            expandedLiterals.add(expandedRule);
        }

        return expandedLiterals;
    }

    /**
     * Generates a mapping of possible enumerations for each literal in allLiterals.
     * Right now this only supports rules (as it expands them into their possible values)
     *
     * @param propLiterals The list of literals (rules and beliefs) marked with [prop]
     * @return A Mapping of original literal to a list of possible enumerations.
     */
    protected Map<LiteralKey, LinkedList<Literal>> generateLiteralEnumerations(Set<Literal> propLiterals) {
        Map<LiteralKey, LinkedList<Literal>> literalMap = new HashMap<>();

        for (Literal lit : propLiterals) {
            // Right now, we are only handling rules, but we can eventually extend support for beliefs
            if (lit.isRule()) {
                // Expand the rule into possible enumerations
                LinkedList<Literal> expandedLiterals = expandRule((Rule) lit);

                // Put the enumerations into the mapping, with the original rule as the key
                literalMap.put(new LiteralKey(lit), expandedLiterals);
            }
        }

        return literalMap;
    }


    /**
     * Generate worlds given a mapping of all propositions. This essentially generates all permutations of each of the possible enumeration values.
     *
     * @param allPropositionsMap This is a mapping of all literals (which are used to create the propositions used in each of the worlds)
     * @return A List of Possible worlds
     */
    protected ManagedWorlds generateWorlds(Map<LiteralKey, LinkedList<Literal>> allPropositionsMap) {

        // Create a blank world. Add it to a list.
        World firstWorld = new World();
        List<World> allWorlds = new LinkedList<>();
        ManagedWorlds managedWorlds = new ManagedWorlds();

        allWorlds.add(firstWorld);


        // Go through each key in the map (aka all literals that go into each world):
        //    For all worlds in the list:
        //      If the world does not contain the predicate indicator, then
        //        Clone the world for each possible value in map for key.
        //      Add each value to their own separate worlds
        //      Add each cloned world to the list.

        // Iterate all "predicates". Each world should have one of the enumeration values from each key.
        for (Map.Entry<LiteralKey, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet()) {
            LiteralKey curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            // Iterate list of current worlds
            ListIterator<World> worldIterator = allWorlds.listIterator();

            while (worldIterator.hasNext()) {
                World world = worldIterator.next();

                // Add one possible enumeration value to the world,
                // cloning the world if there already exists a value for the current key.
                for (Literal val : allLiteralValues) {
                    World nextWorld = world;

                    // Clone the world if we already have a value in this world.
                    if (world.containsKey(curIndicator)) {
                        nextWorld = world.clone();
                    }

                    nextWorld.putLiteral(curIndicator, val);

                    if (!managedWorlds.contains(nextWorld)) {
                        worldIterator.add(nextWorld);
                        managedWorlds.add(nextWorld);
                    }

                }
            }
        }

        // Only keep the worlds that are possible.
        return allWorlds.stream().filter(this::isPossibleWorld).collect(ManagedWorlds.WorldCollector());
    }

    /**
     * Hard-coded to find the current 'is_possible' rule.
     *
     * @return The is_possible rule.
     */
    protected Rule getIsPossibleRule() {
        var rule = this.getTS().getAg().getBB().getCandidateBeliefs(new PredicateIndicator(new Atom("kb"), "is_possible", 3));

        if (rule == null || !rule.hasNext())
            return null;

        return (Rule) rule.next();
    }

    /**
     * Uses the is_possible rule to determine if a world is possible.
     * This essentially injects any variable values into the rule's terms.
     *
     * @param nextWorld The world to check.
     * @return True if the world is possible, false otherwise.
     */
    private boolean isPossibleWorld(World nextWorld) {
        // Get the is_possible rule
        var isPossible = getIsPossibleRule();

        // If no rule is found, all worlds are possible.
        if (isPossible == null)
            return true;

        // Create a unifier
        Unifier unifier = new Unifier();

        // For each of the terms in the rule (i.e. one term would be 'kb::hand("Alice", Hand)'),
        // we want to see if one of the propositions in the world can unify any variables in that term (i.e. Hand).
        // If so, that variable is unified. We continue until all terms are unified. The unified values
        // are stored in the unifier object.
        for (Term t : isPossible.getTerms()) {
            for (Literal lit : nextWorld.values())
                if (unifier.unifies(t, lit))
                    break;
        }

        // We apply the values in the unifier to the rule.
        var isPossibleUnified = (Rule) isPossible.capply(unifier);

        // If there are any un-ground terms in the rule, that means the world does not satisfy the term variables and is therefore not a possible world.
        if (!isPossibleUnified.getHead().isGround() || !isPossibleUnified.getBody().isGround())
            return false;

        // The unified rule is executed to check if the world is possible. If hasNext returns true, then the rule was executed correctly.
        return isPossibleUnified.logicalConsequence(getTS().getAg(), unifier).hasNext();
    }


    public boolean isSelfAgent(Literal l) {
        // Get the source (useful for determining multi-agent accessibility)
        var sourceAgent = l.getSources().getTerm();
        return (sourceAgent.equals(DefaultBeliefBase.ASelf));

//        if(!(sourceAgent instanceof Atom))
//        {
//            System.out.println("source is not an atom: " + sourceAgent.getClass().getSimpleName());
//            return "";
//        }
//        Atom sourceAgAtom = (Atom) sourceAgent;
//
//        return sourceAgAtom.getFunctor();
    }

    private boolean belongsToKB(Literal l) {
        return true;
    }
}
