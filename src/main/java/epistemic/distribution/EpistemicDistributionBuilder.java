package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class EpistemicDistributionBuilder {

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
        var filteredLiterals = processLiterals(epistemicAgent.getBB());

        // Generate the map of literal enumerations
        var literalMap = generateLiteralEnumerations(filteredLiterals);

        // Create the distribution of worlds
        return generateWorlds(literalMap);
    }

    /**
     * Iterates through the belief base, filters the beliefs, and returns the filtered literals/beliefs. Calls {@link EpistemicDistributionBuilder#processLiterals(Iterable, Function[])}.
     * If any of the filters return false for a given belief, it will not be returned. Filters are called in the order
     * that they are passed in.
     *
     * @return A list of filtered literals
     */
    protected abstract List<Literal> processLiterals(Iterable<Literal> literals);

    protected EpistemicAgent getEpistemicAgent()
    {
        return epistemicAgent;
    }

    /**
     * Iterates through the belief base, filters them according to the list of functions, and returns the filtered literals/beliefs.
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
            if (belief == null)
                continue;

            // Process belief through all filters (returns true if no filters)
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

        // Unify each valid unification with the plan head and add it to the belief base.
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
                    if (!t.isGround())
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

                if (prevValues != null) {
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
     * @return A List of Valid worlds
     */
    protected abstract ManagedWorlds generateWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap);
}
