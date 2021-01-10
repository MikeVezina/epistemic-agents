package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.distribution.formula.EpistemicModality;
import epistemic.distribution.generator.*;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class SyntaxDistributionBuilder extends EpistemicDistributionBuilder<String> {

    public static final String NECESSARY_ANNOT = "necessary";
    public static final String POSSIBLY_ANNOT = "possibly";
    private final Logger metricsLogger = Logger.getLogger(getClass().getName() + " - Metrics");
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    protected boolean hasAnnotation(String annotation, Literal literal) {
        return literal.getAnnot(annotation) != null;
    }

    @Override
    protected String acceptsLiteral(Literal literal) {
        // Currently only supports rules (no beliefs...)
        // We do not accept negated rules (doesn't make sense semantically)
        if (!literal.isRule() || literal.negated() || !literal.hasAnnot())
            return null;

        if (hasAnnotation(POSSIBLY_ANNOT, literal))
            return POSSIBLY_ANNOT;

        if (hasAnnotation(NECESSARY_ANNOT, literal))
            return NECESSARY_ANNOT;

        return null;
    }

    @Override
    protected ManagedWorlds processDistribution() {

        BeliefBase beliefBase = getEpistemicAgent().getBB();

        // Get the range definitions
        // Managed literal (as normalized wrapper) -> List of unified values (the range)
        var managedRange = getManagedLiteralRanges(beliefBase);

        // Return no worlds if the range is empty
        if(managedRange.isEmpty())
            return new ManagedWorlds(getEpistemicAgent());

        // NormalizedWrappedLiteral -> All Rules (So that we can execute all rules on worlds)
        // This gives us all the rules for each managed literal (as a normalized wrapper)
        Map<NormalizedWrappedLiteral, List<Rule>> domainRules = findAllDomainRules(beliefBase, managedRange);

        Set<NormalizedWrappedLiteral> independentLiterals = new HashSet<>();
        Set<NormalizedWrappedLiteral> dependentLiterals = new HashSet<>();

        for (var entry : domainRules.entrySet()) {
            if (entry.getValue().isEmpty())
                independentLiterals.add(entry.getKey());
            else
                dependentLiterals.add(entry.getKey());
        }

        var managed = new ManagedWorlds(getEpistemicAgent());
        managed.add(new World());

        // Create worlds for independent propositions
        for (var independent : independentLiterals) {
            List<Literal> possibleValues = managedRange.get(independent);
            var possibleWorldGen = new ExpansionWorldGenerator(getEpistemicAgent(), independent, possibleValues);

            managed = possibleWorldGen.processManagedWorlds(managed);
        }


        // Get queue of dependent rules to process so that we can process them in order
        Queue<WorldGenerator> topology = getTopology(domainRules, managedRange, dependentLiterals);

        while (!topology.isEmpty()) {
            var nextGenerator = topology.poll();
            managed = nextGenerator.processManagedWorlds(managed);
        }


        System.out.println(managed);

        return managed;
    }

    private Queue<WorldGenerator> getTopology(Map<NormalizedWrappedLiteral, List<Rule>> allRules, Map<NormalizedWrappedLiteral, List<Literal>> managedRange, Set<NormalizedWrappedLiteral> dependentLiterals) {
        Queue<WorldGenerator> orderedGenerator = new LinkedList<>();
        Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> ruleDependentMap = new HashMap<>();
        Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> ruleDependeeMap = new HashMap<>();

        // A map that contains the rules that define the conditions for a WrappedLiteral
        Map<WrappedLiteral, Set<Rule>> wrappedRuleMap = new HashMap<>();


        for (var dependent : dependentLiterals) {
            // Only process dependent rules
            var rules = allRules.get(dependent);

            for (var rule : rules) {
                var entry = getRuleDependents(rule, dependentLiterals);
                ruleDependentMap.put(entry.getKey(), entry.getValue());

                for (var dep : entry.getValue()) {
                    ruleDependeeMap.putIfAbsent(dep, new HashSet<>());
                    ruleDependeeMap.get(dep).add(entry.getKey());
                }

                wrappedRuleMap.putIfAbsent(entry.getKey(), new HashSet<>());
                wrappedRuleMap.get(entry.getKey()).add(rule);

            }
        }


        for (var dependent : ruleDependentMap.keySet()) {

            for (var nextRule : wrappedRuleMap.get(dependent)) {
                if(nextRule.negated())
                    continue;

                var nextGenerator = WorldGenerator.createGenerator(getEpistemicAgent(), dependent, nextRule, managedRange.keySet());
                orderedGenerator.add(nextGenerator);
            }

            orderedGenerator.add(new ExpansionWorldGenerator(getEpistemicAgent(), dependent, managedRange.get(dependent)));

            for (var nextRule : wrappedRuleMap.get(dependent)) {
                if(!nextRule.negated())
                    continue;

                var nextGenerator = WorldGenerator.createGenerator(getEpistemicAgent(), dependent, nextRule, managedRange.keySet());
                orderedGenerator.add(nextGenerator);
            }
        }

        return orderedGenerator;
    }


    /**
     * Hooks into the logical consequence function and determines the literals that the rule evaluates.
     * Example: rule(X) :- lit & not other(X,2,3) returns a map {rule(X) -> {lit, other((X,2,3)}
     *
     * @param r The rule to get dependents for.
     * @return A Map entry for the rule and its dependents
     */
    protected Map.Entry<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> getRuleDependents(Rule r, Set<NormalizedWrappedLiteral> dependentLiterals) {
        Set<NormalizedWrappedLiteral> literalList = new HashSet<>();

        var iterator = r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {

            NormalizedWrappedLiteral normalizedLiteral = new NormalizedWrappedLiteral(l);
            for (var norm : dependentLiterals)
                if (norm.canUnify(normalizedLiteral)) {
                    literalList.add(norm);
                    break;
                }

            return List.of(l).listIterator();
        }), new Unifier());

        // Iterate all logical consequences
        while (iterator != null && iterator.hasNext())
            iterator.next();

        for (var norm : dependentLiterals)
            if (norm.canUnify(new NormalizedWrappedLiteral(r.getHead()))) {
                return new AbstractMap.SimpleEntry<>(norm, literalList);
            }

        throw new NullPointerException("??");
    }


    /**
     * Hooks into the logical consequence function and determines the literals that the rule evaluates.
     * Example: rule(X) :- lit & not other(X,2,3) returns a map {rule(X) -> {lit, other((X,2,3)}
     *
     * @param r The rule to get dependents for.
     * @return A Map entry for the rule and its dependents
     */
    protected Map.Entry<WrappedLiteral, Set<WrappedLiteral>> getRuleDependents(Rule r) {
        Set<WrappedLiteral> literalList = new HashSet<>();

        var iterator = r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {
            literalList.add(new WrappedLiteral(l));

            return List.of(l).listIterator();
        }), new Unifier());

        // Iterate all logical consequences
        while (iterator != null && iterator.hasNext())
            iterator.next();

        return new AbstractMap.SimpleEntry<>(new WrappedLiteral(r.getHead()), literalList);
    }


    protected Queue<WorldGenerator> getDependentWorldGenerators(Map<WrappedLiteral, Rule> originalRuleMap, Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals) {
        // Topological sort queue for processing literal generations in order
        Queue<WrappedLiteral> topQueue = new LinkedList<>();

        // The Queue of world generators (in order of dependencies based on top. sort)
        Queue<WorldGenerator> processorChains = new LinkedList<>();

        // The set of literals that belong to the worlds
        Set<WrappedLiteral> worldLiteralMatchers = dependentGroundLiterals.keySet();

        // Add all non-dependent rules to processing queue
        for (var depEntry : dependentKeyLiterals.entrySet()) {
            if (depEntry.getValue().isEmpty())
                topQueue.add(depEntry.getKey());
        }

        // Perform top sort, create world generators for each rule and add them to processing queue.
        while (!topQueue.isEmpty()) {
            var nextKey = topQueue.poll();

            // Get the rule for the next literal
            Rule nextRule = originalRuleMap.get(nextKey);

            // Remove current rule world generator from all dependent rules
            for (var dependent : dependeeKeyLiterals.get(nextKey)) {
                var dependeeList = dependentKeyLiterals.get(dependent);
                dependeeList.remove(nextKey);

                // Add next rule head wrapped literal to queue for processing if all parent dependencies are processed
                if (dependeeList.isEmpty())
                    topQueue.add(dependent);
            }

        }
        return processorChains;
    }


    private Map<NormalizedWrappedLiteral, List<Rule>> findAllDomainRules(BeliefBase beliefBase, Map<NormalizedWrappedLiteral, List<Literal>> managedRange) {

        Map<NormalizedWrappedLiteral, List<Rule>> allRulesMap = new HashMap<>();

        // FInd all relevant rules (i.e. know, ~know, possible(.))
        // Todo: getCandidateBeliefs returns null when nothing is found... handle this
        for (var propLiteral : managedRange.keySet()) {
            allRulesMap.putIfAbsent(propLiteral, new ArrayList<>());
            var allRules = allRulesMap.get(propLiteral);

            var knowledge = propLiteral;
            var negatedKnowledge = new WrappedLiteral(knowledge.getCleanedLiteral().copy().setNegated(Literal.LNeg));
            var possibility = new WrappedLiteral(ASSyntax.createLiteral(EpistemicModality.POSSIBLE.getFunctor(), knowledge.getCleanedLiteral()));


            allRules.addAll(findRules(beliefBase, knowledge));
            allRules.addAll(findRules(beliefBase, negatedKnowledge));
            allRules.addAll(findRules(beliefBase, possibility));

        }

        return allRulesMap;
    }

    private List<Rule> findRules(BeliefBase beliefBase, WrappedLiteral knowledge) {
        var cand = beliefBase.getCandidateBeliefs(knowledge.getCleanedLiteral(), new Unifier());
        List<Rule> allRules = new ArrayList<>();
        if (cand != null) {
            cand.forEachRemaining(l -> {
                if (l.isRule()) {
                    var rule = (Rule) l;
                    if (knowledge.canUnify(new WrappedLiteral(rule.getHead())))
                        allRules.add(rule);
                }

            });
        }

        return allRules;
    }

    public Map<NormalizedWrappedLiteral, List<Literal>> getManagedLiteralRanges(BeliefBase beliefBase) {
        Map<NormalizedWrappedLiteral, List<Literal>> managedRanges = new HashMap<>();

        // Get all range rules:
        var rangeIterator = beliefBase.getCandidateBeliefs(new PredicateIndicator("range", 1));

        if(rangeIterator == null)
        {
            logger.warning("No range definitions found. No epistemic model will be created!");
            return managedRanges;
        }

        rangeIterator.forEachRemaining(rangeLit -> {
            var param = rangeLit.getTerm(0);

            if (!rangeLit.isRule()) {
                logger.warning("Non-rule range: " + rangeLit);
                return;
            }

            if (!param.isLiteral()) {
                logger.warning("Range term is not a literal: " + rangeLit);
                return;
            }

            Rule rangeRule = (Rule) rangeLit;
            var paramLit = (Literal) param;

            if (paramLit.negated())
                logger.warning("Ranges do not support negated literals");

            var prev = managedRanges.put(new NormalizedWrappedLiteral(paramLit), expandRule(rangeRule, paramLit));

            if (prev != null)
                logger.warning("There is more than one definition for the range of " + paramLit.getPredicateIndicator() + ". Any previous definitions will be overwritten.");

        });

        return managedRanges;

    }

    protected List<Literal> expandRule(Rule ruleToProcess, Literal litToUnify) {
        // Obtain the head and body of the rule
        Literal ruleHead = ruleToProcess.getHead();

        // Set up a list of expanded literals
        List<Literal> expandedLiterals = new ArrayList<>();

        if (ruleHead.isGround()) {
            expandedLiterals.add(litToUnify);
            return expandedLiterals;
        }

        // Unify each valid unification with the plan head and add it to the belief base.
        var iter = ruleHead.logicalConsequence(getEpistemicAgent(), new Unifier());

        while (iter.hasNext()) {
            Unifier unif = iter.next();
            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) litToUnify.capply(unif);

            // All unified/expanded rules should be ground.
            if (!expandedRule.isGround()) {
                System.out.println("The expanded range for (" + expandedRule + ") is not ground.");
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


    @Override
    protected ManagedWorlds generateWorlds(Map<String, List<Literal>> allPropositionsMap) {
        // Get rule literals
        var allRules = new ArrayList<Literal>();

        // Merge all value lists to single list
        for (var val : allPropositionsMap.values())
            allRules.addAll(val);

        // Maps a WrappedLiteral rule head to the original rule
        Map<WrappedLiteral, Rule> originalRuleMap = new HashMap<>();

        // Maps WrappedLiteral rule heads to the literals that the rule depends on
        Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals = new HashMap<>();

        // A Mapping of dependencies between rule heads (keys are dependent on values)
        Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals = new HashMap<>();

        // Reverse mapping of above (values are dependent on keys)
        Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals = new HashMap<>();

        // Create Rule topology
        createRuleTopology(allRules, originalRuleMap, dependentGroundLiterals, dependentKeyLiterals, dependeeKeyLiterals);

        // Create world generation queue based on rule topology
        Queue<WorldGenerator> processorChains = getOrderedWorldGenerators(originalRuleMap, dependentGroundLiterals, dependentKeyLiterals, dependeeKeyLiterals);

        ManagedWorlds managedWorlds = new ManagedWorlds(getEpistemicAgent());

        // Warning if a processor was not added (this signals an issue in dependencies between rules)
        if (processorChains.size() != dependeeKeyLiterals.size()) {
            logger.warning("There was a mismatch in rule processing.. could not process some rules");
            logger.warning("Processors: " + processorChains.size() + ", Total Rules: " + dependeeKeyLiterals.size());
            throw new RuntimeException("Failed to create a world generator for a rule (potential issue with rule dependencies)");
        }

        // Process world generators in order
        var blankInitialWorld = new World();
        managedWorlds.add(blankInitialWorld);

        while (!processorChains.isEmpty())
            managedWorlds = processorChains.poll().processManagedWorlds(managedWorlds);

        // Remove the initial world if nothing was generated (it is used only as a starting point for generation)
        if (managedWorlds.size() <= 1 && managedWorlds.contains(blankInitialWorld) && blankInitialWorld.isEmpty())
            managedWorlds.remove(blankInitialWorld);

        return managedWorlds;
    }

    /**
     * Generates the topology for rules.
     *
     * @param allRules                The list of all relevant rules for world generation.
     * @param originalRuleMap         The output map for mapping the wrapped rule head to the original rule
     * @param dependentGroundLiterals The output map for rule dependencies on GROUND wrapped literals
     * @param dependentKeyLiterals    The output map for rule head wrapped literal dependencies on other rule heads (i.e. which rules depend on what)
     * @param dependeeKeyLiterals     The output map for the inverse of the dependentKeyLiterals output
     */
    protected void createRuleTopology(ArrayList<Literal> allRules, Map<WrappedLiteral, Rule> originalRuleMap, Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals) {
        // Initialize dependent mappings for ground literals
        for (var ruleLit : allRules) {
            var rule = (Rule) ruleLit;

            var entry = getRuleDependents(rule);
            dependentGroundLiterals.put(entry.getKey(), entry.getValue());
            originalRuleMap.put(entry.getKey(), rule);
        }


        // Initialize sets
        for (var entry : dependentGroundLiterals.entrySet()) {
            dependentKeyLiterals.put(entry.getKey(), new HashSet<>());
            dependeeKeyLiterals.put(entry.getKey(), new HashSet<>());
        }

        // Goes through all ground literal dependents and finds a matching general rule head
        // This is what links two rules together (as dependents/dependees)
        // This also ignores any literals that are not related to the world generation
        for (var entry : dependentGroundLiterals.entrySet()) {
            var dependentKey = entry.getKey();

            for (var dep : entry.getValue()) {
                for (var key : dependentGroundLiterals.keySet()) {
                    if (dependentKey != key && key.canUnify(dep)) {
                        // Entry.key is dependent on  Key
                        dependentKeyLiterals.get(dependentKey).add(key);
                        dependeeKeyLiterals.get(key).add(dependentKey);
                    }
                }
            }
        }
    }

    @NotNull
    protected Queue<WorldGenerator> getOrderedWorldGenerators(Map<WrappedLiteral, Rule> originalRuleMap, Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals) {
        // Topological sort queue for processing literal generations in order
        Queue<WrappedLiteral> topQueue = new LinkedList<>();

        // The Queue of world generators (in order of dependencies based on top. sort)
        Queue<WorldGenerator> processorChains = new LinkedList<>();

        // The set of literals that belong to the worlds
        Set<WrappedLiteral> worldLiteralMatchers = dependentGroundLiterals.keySet();

        // Add all non-dependent rules to processing queue
        for (var depEntry : dependentKeyLiterals.entrySet()) {
            if (depEntry.getValue().isEmpty())
                topQueue.add(depEntry.getKey());
        }

        // Perform top sort, create world generators for each rule and add them to processing queue.
        while (!topQueue.isEmpty()) {
            var nextKey = topQueue.poll();

            // Get the rule for the next literal
            Rule nextRule = originalRuleMap.get(nextKey);
//
//            if (hasAnnotation(POSSIBLY_ANNOT, nextRule))
//                processorChains.add(new PossiblyGenerator(getEpistemicAgent(), nextRule, worldLiteralMatchers));
//            else if (hasAnnotation(NECESSARY_ANNOT, nextRule))
//                processorChains.add(new NecessaryGenerator(getEpistemicAgent(), nextRule, worldLiteralMatchers));

            // Remove current rule world generator from all dependent rules
            for (var dependent : dependeeKeyLiterals.get(nextKey)) {
                var dependeeList = dependentKeyLiterals.get(dependent);
                dependeeList.remove(nextKey);

                // Add next rule head wrapped literal to queue for processing if all parent dependencies are processed
                if (dependeeList.isEmpty())
                    topQueue.add(dependent);
            }

        }
        return processorChains;
    }


}
