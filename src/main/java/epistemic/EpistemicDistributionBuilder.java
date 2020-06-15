package epistemic;

import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class EpistemicDistributionBuilder {

    public static final Atom KB = ASSyntax.createAtom("kb");
    public static final Atom PROP_ANNOT = ASSyntax.createAtom("prop");
    public static final String IS_POSSIBLE_RULE_FUNCTOR = "is_possible";
    private EpistemicAgent epistemicAgent;

    /**
     * Should be called once the agent has been loaded.
     * This method needs to process the belief base as a whole
     * after the initial agent has been loaded and processed.
     * This is required in order for logicalConsequences to work
     * correctly.
     *
     * @param agent Necessary for accessing BB rules and agent
     *              logical consequences for evaluation and expansion.
     */
    @NotNull
    public EpistemicDistribution createDistribution(@NotNull EpistemicAgent agent) {
        this.epistemicAgent = agent;

        var managedWorlds = processDistribution();
        System.out.println(managedWorlds.toString());

        return new EpistemicDistribution(this.epistemicAgent, managedWorlds);
    }


    /**
     * Process the distribution of worlds, create, and set the ManagedWorlds object.
     */
    protected ManagedWorlds processDistribution() {
        // Gets and processes all literals in the kb belief base that are marked with 'prop'
        var filteredLiterals = processLiterals(this::nsFilter, this::propFilter);

        // Generate the map of literal enumerations
        var literalMap = generateLiteralEnumerations(filteredLiterals);

        // Create the distribution of worlds
        return generateWorlds(literalMap);
    }

    private Boolean nsFilter(Literal literal) {
        return literal.getNS().equals(KB);
    }


    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private boolean propFilter(Literal literal) {
        return literal.hasAnnot(PROP_ANNOT);
    }

    /**
     * Iterates through the belief base, filters the beliefs, and returns the filtered literals/beliefs. Calls {@link EpistemicDistributionBuilder#processLiterals(Iterable, Function[])}.
     * If any of the filters return false for a given belief, it will not be returned. Filters are called in the order
     * that they are passed in.
     *
     * @return A list of filtered literals
     */
    @SafeVarargs
    protected final List<Literal> processLiterals(Function<Literal, Boolean>... filters) {
        return this.processLiterals(epistemicAgent.getBB(), filters);
    }

    /**
     * Iterates through the belief base (kb namespace only), filters them, and returns the filtered literals/beliefs.
     * If any of the filters return false for a given belief, it will not be returned. Filters are called in the order
     * that they are passed in.
     *
     * @return A list of filtered literals
     */
    @SafeVarargs
    protected final List<Literal> processLiterals(Iterable<Literal> literals, Function<Literal, Boolean>... filters) {
        // We need to iterate all beliefs.
        // We can't use beliefBase.getCandidateBeliefs(...) [aka the pattern matching function] because
        // the pattern matching function doesn't allow us to pattern match by just namespace and annotation
        // (it requires a functor and arity)
        List<Literal> filteredLiterals = new ArrayList<>();

        // Iterate through the belief base and call the consumers
        for (Literal belief : literals) {
            if (belief == null || !belief.getNS().equals(KB))
                continue;

            // Process belief through all filters
            var allFilterMatch = Arrays.stream(filters).allMatch((filter) -> filter.apply(belief));

            if (allFilterMatch)
                filteredLiterals.add(belief);

        }

        return filteredLiterals;
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
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(this.epistemicAgent, new Unifier());

        // Set up a list of expanded literals
        LinkedList<Literal> expandedLiterals = new LinkedList<>();

        // Unify each possible unification with the plan head and add it to the belief base.
        while (unifIterator.hasNext()) {
            Unifier unif = unifIterator.next();

            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) ruleHead.capply(unif);
            System.out.println("Unifying " + rule.getFunctor() + " with " + unif + ". Result: " + expandedRule);

            // All unified/expanded rules should be ground.
            if (!expandedRule.isGround()) {
                System.out.println("The expanded rule (" + expandedRule + ") is not ground.");
                for (int i = 0; i < expandedRule.getArity(); i++) {
                    Term t = expandedRule.getTerm(i);
                    if(!t.isGround())
                        System.out.println("Term " + t + " is not ground.");
                }
            }

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
    protected Map<WrappedLiteral, LinkedList<Literal>> generateLiteralEnumerations(List<Literal> propLiterals) {
        Map<WrappedLiteral, LinkedList<Literal>> literalMap = new HashMap<>();

        for (Literal lit : propLiterals) {
            // Right now, we are only handling rules, but we can eventually extend support for beliefs
            if (lit.isRule()) {
                // Expand the rule into possible enumerations
                LinkedList<Literal> expandedLiterals = expandRule((Rule) lit);

                // Put the enumerations into the mapping, with the original rule as the key
                var wrappedKey = new WrappedLiteral(lit);
                var prevValues = literalMap.put(wrappedKey, expandedLiterals);

                if (prevValues != null)
                {
                    epistemicAgent.getLogger().warning("There is an enumeration collision for the key: " + wrappedKey.getCleanedLiteral());
                    epistemicAgent.getLogger().warning("The following enumeration values have been overwritten: " + prevValues);
                }

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
    protected ManagedWorlds generateWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap) {

        // Create a blank world. Add it to a list.
        World firstWorld = new World();
        List<World> allWorlds = new LinkedList<>();
        ManagedWorlds managedWorlds = new ManagedWorlds(epistemicAgent);

        allWorlds.add(firstWorld);


        // Go through each key in the map (aka all literals that go into each world):
        //    For all worlds in the list:
        //      If the world does not contain the predicate indicator, then
        //        Clone the world for each possible value in map for key.
        //      Add each value to their own separate worlds
        //      Add each cloned world to the list.

        // Iterate all "predicates". Each world should have one of the enumeration values from each key.
        for (Map.Entry<WrappedLiteral, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
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

                    Proposition newProp = new Proposition(curIndicator, new WrappedLiteral(val));
                    nextWorld.putProposition(newProp);

                    if (!allWorlds.contains(nextWorld))
                        worldIterator.add(nextWorld);

                }
            }
        }

        // Add all worlds to the managed worlds
        // We can't add to managedWorlds during the above loop because:
        // We modify World instances that exist in the allWorlds list. ManagedWorlds (hashset) wouldn't like that because
        // hashsets can't handle changes in an existing entry's hashcode (resulting in duplicate entries of the same instance).
        managedWorlds.addAll(allWorlds);



        // Only keep the worlds that are possible.
        return allWorlds.stream().filter(this::isPossibleWorld).collect(ManagedWorlds.WorldCollector(epistemicAgent));
    }

    /**
     * Hard-coded to find the current 'is_possible' rule.
     *
     * @return The is_possible rule.
     */
    protected Rule getIsPossibleRule() {
        var rule = this.epistemicAgent.getBB().getCandidateBeliefs(new PredicateIndicator(KB, IS_POSSIBLE_RULE_FUNCTOR, 3));

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
    protected boolean isPossibleWorld(World nextWorld) {
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
            if (!t.isLiteral())
                continue;

            WrappedLiteral wrappedTerm = new WrappedLiteral((Literal) t).getNormalizedWrappedLiteral();
            for (var lit : nextWorld.valueSet()) {
                // Unify the rule terms until we find a valid unification
                var unification = wrappedTerm.unifyWrappedLiterals(lit.getValue());
                if (unification != null) {
                    unifier.compose(unification);
                    break;
                }
            }
        }

        // We apply the values in the unifier to the rule.
        var isPossibleUnified = (Rule) isPossible.capply(unifier);

        // If there are any un-ground terms in the rule, that means the world does not satisfy the term variables and is therefore not a possible world.
        if (!isPossibleUnified.getHead().isGround() || !isPossibleUnified.getBody().isGround())
            return false;

        // The unified rule is executed to check if the world is possible. If hasNext returns true, then the rule was executed correctly.
        return isPossibleUnified.logicalConsequence(epistemicAgent, unifier).hasNext();
    }


}
