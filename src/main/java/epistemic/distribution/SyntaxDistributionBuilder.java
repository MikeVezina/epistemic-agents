package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SyntaxDistributionBuilder extends EpistemicDistributionBuilder {

    public static final String UNKNOWN_ANNOT = "unknown";
    private static final String KNOWN_ANNOT = "known";

    private final Logger metricsLogger = Logger.getLogger(getClass().getName() + " - Metrics");
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    protected List<Literal> processLiterals(Iterable<Literal> literals) {
        var listLiterals = processLiterals(literals, this::unknownFilter);
        listLiterals.addAll(processLiterals(literals, this::knownFilter));

        return listLiterals;
    }

    private Boolean knownFilter(Literal literal) {
        return literal.getAnnot(KNOWN_ANNOT) != null;
    }

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private boolean unknownFilter(Literal literal) {
        return literal.getAnnot(UNKNOWN_ANNOT) != null;
    }

    @Override
    protected LinkedList<Literal> expandRule(Rule rule) {
        return this.expandRule(rule, getEpistemicAgent());
    }

    @Override
    protected ManagedWorlds processDistribution() {
        // Get rule literals
        var allRules = processLiterals(getEpistemicAgent().getBB());

        // Filter 'unknown' literals
        var unknownRules = allRules.stream().filter(this::unknownFilter).collect(Collectors.toList());
        var knownRules = allRules.stream().filter(this::knownFilter).collect(Collectors.toList());
        var initialManaged = createManagedFromUnknown(unknownRules);

        extendWorldsFromKnown(initialManaged, knownRules);



        return initialManaged;
    }

    private void extendWorldsFromKnown(ManagedWorlds initialManaged, List<Literal> knownRules) {

        WorldLogicalConsequence worldAgent = new WorldLogicalConsequence(getEpistemicAgent(), initialManaged);

        for(World world : initialManaged) {
            worldAgent.setEvaluationWorld(world);
            for (var knownRule : knownRules) {
                if (!knownRule.isRule())
                    continue;

                Rule rule = (Rule) knownRule;
                // Unify all world rules with the Belief Base (which should give us all possibilities)
                var unifiedRules = unifyRules(rule);

                for (var nextRule : unifiedRules) {
                    var iter = (nextRule.getBody().logicalConsequence(worldAgent, new Unifier()));

                    if(iter == null)
                        continue;

                    while(iter.hasNext())
                    {
                        Unifier next = iter.next();
                        Literal newLiteral = (Literal) nextRule.getHead().capply(next);
                        world.putProposition(initialManaged.getManagedProposition(newLiteral));
                        System.out.println(newLiteral);
                    }

                }
            }
        }
    }

    protected ManagedWorlds createManagedFromUnknown(List<Literal> unknownRules) {

        if (unknownRules.size() != 1)
            throw new RuntimeException("Multiple unknown rules not supported...");

        ManagedWorlds managedWorlds = new ManagedWorlds(getEpistemicAgent());

        for (var unknown : unknownRules) {
            if (!unknown.isRule())
                continue;

            Rule rule = (Rule) unknown;
            var lits = expandRule(rule);

            for (var lit : lits) {
                var newWorld = new World();
                newWorld.putLiteral(new WrappedLiteral(unknown), lit);
                managedWorlds.add(newWorld);
            }
            System.out.println(lits);
        }
        return managedWorlds;
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
    protected LinkedList<Literal> expandRule(Rule rule, Agent logConsAgent) {
        // Obtain the head and body of the rule
        Literal ruleHead = rule.getHead();
        LogicalFormula ruleBody = rule.getBody();

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(logConsAgent, new Unifier());

        // Set up a list of expanded literals
        LinkedList<Literal> expandedLiterals = new LinkedList<>();

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

    protected List<Rule> unifyRules(Rule rule) {
        // Obtain the head and body of the rule
        Literal ruleHead = rule.getHead();
        LogicalFormula ruleBody = rule.getBody();

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(getEpistemicAgent(), new Unifier());

        // Set up a list of expanded literals
        LinkedList<Rule> unifiedRules = new LinkedList<>();

        // Unify each valid unification with the plan head and add it to the belief base.
        while (unifIterator.hasNext()) {
            Unifier unif = unifIterator.next();

            // Clone and apply the unification to the rule head
            Literal expandedRuleHead = (Literal) ruleHead.capply(unif);
            LogicalFormula unifiedBody = (LogicalFormula) ruleBody.capply(unif);
            Rule expandedRule = new Rule(expandedRuleHead, unifiedBody);

            System.out.println("Unifying " + ruleHead.toString() + " with " + unif + ". Result: " + expandedRule);
            unifiedRules.add(expandedRule);
        }

        return unifiedRules;
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
                Rule litRule = (Rule) lit;

                // Expand the rule into possible enumerations (these are our worlds)
                LinkedList<Literal> expandedLiterals = expandRule(litRule);

                // Put the enumerations into the mapping, with the original rule as the key
                var wrappedKey = new WrappedLiteral(lit);
                var prevValues = literalMap.put(wrappedKey, expandedLiterals);

                if (prevValues != null) {
                    getEpistemicAgent().getLogger().warning("There is an enumeration collision for the key: " + wrappedKey.getCleanedLiteral());
                    getEpistemicAgent().getLogger().warning("The following enumeration values have been overwritten: " + prevValues);
                }
            }
        }

        return literalMap;
    }


    /**
     * Generate worlds given a mapping of all propositions. This essentially generates all permutations of each of the possible enumeration values.
     *
     * @param allPropositionsMap This is a mapping of world enumerations
     * @return A List of Valid worlds
     */
    protected ManagedWorlds generateWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap) {

        Map<WrappedLiteral, List<Literal>> newWorldRules = allPropositionsMap.entrySet().stream().filter(e -> unknownFilter(e.getKey().getOriginalLiteral())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<WrappedLiteral, List<Literal>> extendWorldRules = allPropositionsMap.entrySet().stream().filter(e -> knownFilter(e.getKey().getOriginalLiteral())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Create new worlds (the ones marked with '[unknown]')
        List<World> allWorlds = createNewWorlds(newWorldRules);

        ManagedWorlds baseWorlds = allWorlds.stream().collect(ManagedWorlds.WorldCollector(getEpistemicAgent()));

        // Extend worlds with matching rules
        expandExistingWorlds(baseWorlds, extendWorldRules);

        // Only keep the worlds that are valid.
        return baseWorlds;
    }

    private void expandExistingWorlds(ManagedWorlds baseWorlds, Map<WrappedLiteral, List<Literal>> extendWorldRules) {

        for (Map.Entry<WrappedLiteral, List<Literal>> predEntry : extendWorldRules.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            List<Literal> allLiteralValues = predEntry.getValue();

            for (var litValues : allLiteralValues) {

            }
        }
    }

    private World createWorldFromGroundLiteral(@NotNull WrappedLiteral indicator, @NotNull Literal groundLiteral) {
        assert groundLiteral.isGround();

        // Separate the terms in the world indicator
        List<Term> terms = indicator.getCleanedLiteral().getTerms();

        // Skip any matchers that don't have any terms (?)
        if (terms.isEmpty()) {
            logger.warning("World Indicator has no terms: " + indicator);
        }

        World newWorld = new World();

        // Insert proposition into world
        newWorld.putLiteral(indicator, groundLiteral);


        return newWorld;
    }


    private List<World> createNewWorlds(Map<WrappedLiteral, List<Literal>> newWorldRules) {
        List<World> allWorlds = new LinkedList<>();

        long totalExtendTime = 0;
        long totalCreationTime = System.nanoTime();

        // Create all worlds ([world] annotations)
        for (Map.Entry<WrappedLiteral, List<Literal>> predEntry : newWorldRules.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            List<Literal> allLiteralValues = predEntry.getValue();

            for (var litValues : allLiteralValues) {
                World curWorld = createWorldFromGroundLiteral(curIndicator, litValues);
                allWorlds.add(curWorld);
            }
        }

        totalCreationTime = System.nanoTime() - totalCreationTime;

        metricsLogger.info("Total Creation Time (ms): " + totalCreationTime / 1000000);
        metricsLogger.info("Total Extend Time (ms): " + totalExtendTime / 1000000);

        return allWorlds;
    }

}
