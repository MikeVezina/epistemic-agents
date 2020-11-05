package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.distribution.processor.EvaluatorWorld;
import epistemic.distribution.processor.NecessaryWorld;
import epistemic.distribution.processor.PossiblyWorld;
import epistemic.distribution.processor.WorldProcessorChain;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SyntaxDistributionBuilder extends EpistemicDistributionBuilder {

    public static final String NECESSARY_ANNOT = "necessary";
    public static final String POSSIBLY_ANNOT = "possibly";
    private final Logger metricsLogger = Logger.getLogger(getClass().getName() + " - Metrics");
    private final Logger logger = Logger.getLogger(getClass().getName());

    private Boolean possiblyFilter(Literal literal) {
        return literal.getAnnot(POSSIBLY_ANNOT) != null;
    }

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private boolean necessaryFilter(Literal literal) {
        return literal.getAnnot(NECESSARY_ANNOT) != null;
    }

    @Override
    protected ManagedWorlds processDistribution() {

        // Get rule literals
        var allRules = processLiterals(getEpistemicAgent().getBB(), this::possiblyFilter, this::possiblyFilter);

        Map<WrappedLiteral, Rule> originalRuleMap = new HashMap<>();

        Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals = new HashMap<>();

        for (var ruleLit : allRules) {
            var rule = (Rule) ruleLit;

            var entry = getRuleDependents(rule);
            dependentGroundLiterals.put(entry.getKey(), entry.getValue());
            originalRuleMap.put(entry.getKey(), rule);
        }

        // A Mapping of dependencies between rule heads (keys are dependent on values)
        Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals = new HashMap<>();

        // Reverse mapping of above (values are dependent on keys)
        Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals = new HashMap<>();

        for (var entry : dependentGroundLiterals.entrySet()) {
            dependentKeyLiterals.put(entry.getKey(), new HashSet<>());
            dependeeKeyLiterals.put(entry.getKey(), new HashSet<>());
        }

        for (var entry : dependentKeyLiterals.entrySet()) {
            var dependentKey = entry.getKey();

            for (var dep : entry.getValue()) {
                for (var key : dependentGroundLiterals.keySet()) {
                    if (key.canUnify(dep)) {
                        // Entry.key is dependent on  Key
                        dependentKeyLiterals.get(dependentKey).add(key);
                        dependeeKeyLiterals.get(key).add(dependentKey);
                    }
                }
            }
        }

        Queue<WrappedLiteral> topQueue = new LinkedList<>();

        Queue<WorldProcessorChain> processorChains = new LinkedList<>();
        Set<WrappedLiteral> worldLiteralMatchers = dependentGroundLiterals.keySet();


        // Add all non-dependent rules to processing queue
        for (var depEntry : dependentKeyLiterals.entrySet()) {
            if (depEntry.getValue().isEmpty())
                topQueue.add(depEntry.getKey());
        }

        while (!topQueue.isEmpty()) {
            var nextKey = topQueue.poll();

            // Get the rule for the next literal
            Rule nextRule = originalRuleMap.get(nextKey);

            var nextProcessor = new PossiblyWorld(getEpistemicAgent(), nextRule, worldLiteralMatchers);
            processorChains.add(nextProcessor);


            for (var dependent : dependeeKeyLiterals.get(nextKey)) {
                var dependeeList = dependeeKeyLiterals.get(dependent);
                dependeeList.remove(nextKey);
                if (dependeeList.isEmpty())
                    topQueue.add(dependent);
            }

        }


        ManagedWorlds managedWorlds = new ManagedWorlds(getEpistemicAgent());
        managedWorlds.add(new World());

        if(processorChains.size() != dependeeKeyLiterals.size())
        {
            logger.warning("There was a mismatch in rule processing.. could not process some rules");
        }

        while (!processorChains.isEmpty())
            managedWorlds = processorChains.poll().processManagedWorlds(managedWorlds);

        return managedWorlds;

    }

    @Override
    protected List<Literal> processLiterals(Iterable<Literal> literals) {
        return new ArrayList<>();
    }

    private Map.Entry<WrappedLiteral, Set<WrappedLiteral>> getRuleDependents(Rule r) {
        Set<WrappedLiteral> literalList = new HashSet<>();

        r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {
            literalList.add(new WrappedLiteral(l));
            return null;
        }), new Unifier());

        return new AbstractMap.SimpleEntry<>(new WrappedLiteral(r.getHead()), literalList);
    }

    private void extendWorldsFromKnown(ManagedWorlds initialManaged, List<Literal> knownRules) {

        WorldLogicalConsequence worldAgent = new WorldLogicalConsequence(getEpistemicAgent(), initialManaged);
        List<Rule> knownUnifiedRules = new ArrayList<>();

        // Unify the known rules with the belief base first.. this gets all possible unifications for all worlds
        for (var knownRule : knownRules) {
            if (!knownRule.isRule())
                continue;

            Rule rule = (Rule) knownRule;

            // Unify all world rules with the Belief Base (which should give us all possibilities)
            knownUnifiedRules.addAll(unifyRules(rule));
        }

        // Determine which rules are applicable to each world, and if so, introduce the ground rule head
        for (World world : initialManaged) {
            worldAgent.setEvaluationWorld(world);
            for (var nextRule : knownUnifiedRules) {
                var iter = nextRule.getBody().logicalConsequence(worldAgent, new Unifier());

                if (iter == null || !iter.hasNext())
                    continue;

                while (iter.hasNext()) {
                    Unifier next = iter.next();
                    Literal newLiteral = (Literal) nextRule.getHead().capply(next);
                    Proposition newProp = new Proposition(new WrappedLiteral(newLiteral), newLiteral);
                    world.putProposition(newProp);
                    initialManaged.getManagedLiterals().addManagedProposition(newProp);
                    System.out.println(newLiteral);
                }

                System.out.println();
            }
        }
    }

    protected ManagedWorlds createManagedFromRule(Rule rule) {

        ManagedWorlds managedWorlds = new ManagedWorlds(getEpistemicAgent());

        var lits = expandRule(rule);

        for (var lit : lits) {
            var newWorld = new World();
            newWorld.putLiteral(new WrappedLiteral(rule.getHead()), lit);
            managedWorlds.add(newWorld);
        }

        System.out.println(lits);

        return managedWorlds;
    }


    protected List<Rule> unifyRules(Rule rule) {
        // Obtain the head and body of the rule
        Literal ruleHead = rule.getHead();
        LogicalFormula ruleBody = rule.getBody();

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(getEpistemicAgent(), new Unifier());

        // Set up a list of expanded literals
        LinkedList<Rule> unifiedRules = new LinkedList<>();

        // Add original rule
        if (unifIterator == null || !unifIterator.hasNext())
            unifiedRules.add(rule.clone());

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
