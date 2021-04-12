package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.distribution.formula.EpistemicFormula;
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
import java.util.stream.Collectors;

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
        var startTime = System.currentTimeMillis();

        // Get the range definitions
        // Managed literal (as normalized wrapper) -> List of unified values (the range)
        var managedRange = getManagedLiteralRanges(beliefBase);

        var rangeTime = System.currentTimeMillis();
        metricsLogger.info("Range generation time: " + (rangeTime - startTime) + " ms");

        // Return no worlds if the range is empty
        if (managedRange.isEmpty())
            return new ManagedWorlds(getEpistemicAgent());

        // NormalizedWrappedLiteral -> All Rules (So that we can execute all rules on worlds)
        // This gives us all the rules for each managed literal (as a normalized wrapper)
        Map<NormalizedWrappedLiteral, List<Rule>> domainRules = findAllDomainRules(beliefBase, managedRange);

        var domainRuleTime = System.currentTimeMillis();
        metricsLogger.info("Find Domain Rules Time: " + (domainRuleTime - rangeTime) + " ms");


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

            logger.info("Generating Worlds for independent propositions: " + independent);
            managed = possibleWorldGen.processManagedWorlds(managed);
        }

        var independentTime = System.currentTimeMillis();
        metricsLogger.info("Generate Independent (Range-only) Proposition Worlds Time: " + (independentTime - domainRuleTime) + " ms");

        // Get queue of dependent rules to process so that we can process them in order
        Queue<WorldGenerator> topology = getTopology(domainRules, managedRange, dependentLiterals);

        boolean hasDependent = !topology.isEmpty();

        while (!topology.isEmpty()) {
            var nextGenerator = topology.poll();
            logger.info("Generating: " + nextGenerator.getClass().getName());
            managed = nextGenerator.processManagedWorlds(managed);
        }

        if (hasDependent) {
            var dependentTime = System.currentTimeMillis();
            metricsLogger.info("Generate Dependent (Knowledge Rule) Proposition Worlds Time: " + (dependentTime - independentTime) + " ms");
        }

        metricsLogger.info("Total World Generation Time (" + managed.size() + " Worlds, " + managed.getManagedLiterals().size() + " Propositions): " + (System.currentTimeMillis() - startTime) + " ms");
        return managed;
    }

    private Queue<WorldGenerator> getTopology(Map<NormalizedWrappedLiteral, List<Rule>> allRules, Map<NormalizedWrappedLiteral, List<Literal>> managedRange, Set<NormalizedWrappedLiteral> dependentLiterals) {
        Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> propDependentMap = new HashMap<>();
        Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> propDependeeMap = new HashMap<>();


        // Keep track of rules that depend on their own dependent literal (these need to be processed last!)
        Map<NormalizedWrappedLiteral, Set<Rule>> selfDependentRules = new HashMap<>();
        Map<NormalizedWrappedLiteral, Set<Rule>> selfIndependentRules = new HashMap<>();

        // Map out dependency topology based on rule dependents
        for (var dependent : dependentLiterals) {
            // Only process dependent rules
            var rules = allRules.get(dependent);

            for (var rule : rules) {
                var entry = getRuleDependents(rule, dependentLiterals, managedRange);
                propDependentMap.put(entry.getKey(), entry.getValue());

                // Add to rules that depend on their own literal values
                if (entry.getValue().contains(dependent)) {
                    selfDependentRules.putIfAbsent(dependent, new HashSet<>());
                    selfDependentRules.get(dependent).add(rule);
                } else {
                    selfIndependentRules.putIfAbsent(dependent, new HashSet<>());
                    selfIndependentRules.get(dependent).add(rule);
                }

                propDependeeMap.putIfAbsent(dependent, new HashSet<>());

                for (var dep : entry.getValue()) {
                    propDependeeMap.putIfAbsent(dep, new HashSet<>());
                    propDependeeMap.get(dep).add(entry.getKey());
                }

            }
        }


        return getOrderedWorldGenerators(allRules, managedRange, propDependentMap, propDependeeMap, selfDependentRules, selfIndependentRules);
    }

    @NotNull
    private Queue<WorldGenerator> getOrderedWorldGenerators(Map<NormalizedWrappedLiteral, List<Rule>> allRules, Map<NormalizedWrappedLiteral, List<Literal>> managedRange, Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> propDependentMap, Map<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> propDependeeMap, Map<NormalizedWrappedLiteral, Set<Rule>> selfDependentRulesMap, Map<NormalizedWrappedLiteral, Set<Rule>> selfIndependentRulesMap) {
        // Go through high level dependencies (i.e. alice cards depends on Bob cards)
        Queue<WorldGenerator> orderedGenerator = new LinkedList<>();

        Queue<NormalizedWrappedLiteral> topSortQueue = new LinkedList<>();
        Set<NormalizedWrappedLiteral> visited = new HashSet<>();

        // Add all independent or self-dependent prop rules
        for (var depEntry : propDependeeMap.entrySet()) {
            if (depEntry.getValue().isEmpty() || depEntry.getValue().contains(depEntry.getKey()))
                topSortQueue.add(depEntry.getKey());
        }


        while (!topSortQueue.isEmpty()) {
            var currentManagedLiteralKey = topSortQueue.poll();

            if (visited.contains(currentManagedLiteralKey))
                continue;

            visited.add(currentManagedLiteralKey);

            // Create ordered world generators for the rules for this specific managed literal key
            orderedGenerator.addAll(createOrderedRuleGenerators(allRules, managedRange, selfDependentRulesMap, selfIndependentRulesMap, currentManagedLiteralKey));


            for (var nextDep : propDependentMap.get(currentManagedLiteralKey)) {
                var nextDeps = propDependeeMap.get(nextDep);
                nextDeps.remove(currentManagedLiteralKey);

                if (nextDeps.isEmpty() && !visited.contains(nextDep))
                    topSortQueue.add(nextDep);
            }

        }

        return orderedGenerator;
    }

    private Queue<WorldGenerator> createOrderedRuleGenerators(Map<NormalizedWrappedLiteral, List<Rule>> allRules, Map<NormalizedWrappedLiteral, List<Literal>> managedRange, Map<NormalizedWrappedLiteral, Set<Rule>> selfDependentRulesMap, Map<NormalizedWrappedLiteral, Set<Rule>> selfIndependentRulesMap, NormalizedWrappedLiteral currentManagedLiteralKey) {

        Queue<WorldGenerator> orderedGenerators = new LinkedList<>();
        // Iterate all rules for current proposition
        // Separates rules into mappings based on whether they add or remove propositions from worlds
        // The mapping also gives us the EpistemicFormula to use for the rule head.
        Set<Rule> additionRules = new HashSet<>();
        Set<Rule> removalRules = new HashSet<>();

        // Sorts the rules into the above sets (used as output parameters)
        gatherAdditionRemovalRules(allRules, currentManagedLiteralKey, additionRules, removalRules);

        /*
            Add World generators based on the following sequence (order matters here):
            1. Proposition addition (self-independent)
            2. General Addition (i.e. expand worlds without a value)
            3. Proposition Removal (self-independent)
            4. Proposition Addition (self-dependent)
            5. Proposition removal (self-dependent)
         */

        // 1. Independent Prop Addition (intersection of addition and self independent rules)
        var independentRules = selfIndependentRulesMap.get(currentManagedLiteralKey);

        if (independentRules != null && !independentRules.isEmpty()) {
            var independentAddition = additionRules.stream().filter(independentRules::contains).collect(Collectors.toSet());
            orderedGenerators.addAll(createdGeneratorQueue(currentManagedLiteralKey, independentAddition, managedRange));

            // 2. General Addition (Fill remaining worlds with propositions)
            orderedGenerators.add(new ExpansionWorldGenerator(getEpistemicAgent(), currentManagedLiteralKey, managedRange.get(currentManagedLiteralKey)));

            // 3. Independent Prop Removal (intersection of removal and self independent rules)
            var independentRemoval = removalRules.stream().filter(independentRules::contains).collect(Collectors.toSet());
            orderedGenerators.addAll(createdGeneratorQueue(currentManagedLiteralKey, independentRemoval, managedRange));
        } else {
            // Make sure we still run the general addition if no independent rules!!
            orderedGenerators.add(new ExpansionWorldGenerator(getEpistemicAgent(), currentManagedLiteralKey, managedRange.get(currentManagedLiteralKey)));
        }

        // Process Dependent Rules
        var dependentRules = selfDependentRulesMap.get(currentManagedLiteralKey);

        if (dependentRules != null && !dependentRules.isEmpty()) {
            // 4. Dependent Prop Addition (intersection of addition and self dependent rules)
            var dependentAddition = additionRules.stream().filter(dependentRules::contains).collect(Collectors.toSet());
            orderedGenerators.addAll(createdGeneratorQueue(currentManagedLiteralKey, dependentAddition, managedRange));

            // Independent Prop removal (intersection of removal and self dependent rules)
            var dependentRemoval = removalRules.stream().filter(dependentRules::contains).collect(Collectors.toSet());
            orderedGenerators.addAll(createdGeneratorQueue(currentManagedLiteralKey, dependentRemoval, managedRange));
        }

        return orderedGenerators;
    }

    private void gatherAdditionRemovalRules(Map<NormalizedWrappedLiteral, List<Rule>> allRules, NormalizedWrappedLiteral currentManagedLiteralKey, Set<Rule> additionRules, Set<Rule> removalRules) {
        // Add to addition/removal mapping based on formula for rule head
        for (var nextRule : allRules.get(currentManagedLiteralKey)) {
            var formula = EpistemicFormula.fromLiteral(nextRule.getHead());

            if (isPositiveFormula(formula) || isPossibleDoubleNegative(formula))
                additionRules.add(nextRule);
            else
                removalRules.add(nextRule);
        }
    }

    private Queue<WorldGenerator> createdGeneratorQueue(NormalizedWrappedLiteral propDependent, Set<Rule> rules, Map<NormalizedWrappedLiteral, List<Literal>> managedRangeLiterals) {
        Queue<WorldGenerator> generatorQueue = new LinkedList<>();

        // Add all prop additions to the queue first
        // i.e. rule heads with positive knowledge, positive possibility (and prop), and negative possibilities with a negative prop.
        for (var rule : rules)
            generatorQueue.add(WorldGenerator.createGenerator(getEpistemicAgent(), propDependent, rule, managedRangeLiterals.keySet()));

        return generatorQueue;
    }

    private boolean isPossibleDoubleNegative(EpistemicFormula formula) {
        return formula.getEpistemicModality().equals(EpistemicModality.POSSIBLE) && formula.isModalityNegated() && formula.isPropositionNegated();
    }

    private boolean isPositiveFormula(EpistemicFormula formula) {
        return !formula.isModalityNegated() && !formula.isPropositionNegated();
    }


    /**
     * Gets the dependent managed literals of a rule. This hasn't been tested much but I know there's issues with hooking into logicalConsequences (since
     *
     * @param r The rule to get dependents for.
     * @return A Map entry that maps the rule's dependent literal to a set of other dependent literals (both the key and values belong to the dependentLiterals set)
     */
    protected Map.Entry<NormalizedWrappedLiteral, Set<NormalizedWrappedLiteral>> getRuleDependents(Rule r, Set<NormalizedWrappedLiteral> dependentLiterals, Map<NormalizedWrappedLiteral, List<Literal>> managedRange) {
        Set<NormalizedWrappedLiteral> literalList = new HashSet<>();

        var iterator = r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {

            NormalizedWrappedLiteral normalizedLiteral = new NormalizedWrappedLiteral(l);
            for (var norm : dependentLiterals)
                if (norm.canUnify(normalizedLiteral)) {
                    literalList.add(norm);
                    break;
                }

            // TODO: This may not work for dependent literals because the values are generated by a range(.) rule which won't be picked up by this.
            return getEpistemicAgent().getBB().getCandidateBeliefs(l, u);
        }), new Unifier());

        // Iterate all logical consequences
        while (iterator != null && iterator.hasNext())
            iterator.next();

        EpistemicFormula ruleHeadFormula = EpistemicFormula.fromLiteral(r.getHead());

        for (var norm : dependentLiterals)
            if (norm.canUnify(ruleHeadFormula.getRootLiteral().getNormalizedWrappedLiteral())) {
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
    @Deprecated
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

    @Deprecated
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
        for (var knowledge : managedRange.keySet()) {

            // Place new array list in map if not exist
            allRulesMap.putIfAbsent(knowledge, new ArrayList<>());
            var allRules = allRulesMap.get(knowledge);

            var negatedKnowledge = new WrappedLiteral(knowledge.getCleanedLiteral().copy().setNegated(Literal.LNeg));
            var possibility = new WrappedLiteral(ASSyntax.createLiteral(EpistemicModality.POSSIBLE.getFunctor(), knowledge.getCleanedLiteral()));
            var negatedPossibility = new WrappedLiteral(possibility.getCleanedLiteral().copy().setNegated(Literal.LNeg));


            allRules.addAll(findRules(beliefBase, knowledge));
            allRules.addAll(findRules(beliefBase, negatedKnowledge));
            allRules.addAll(findRules(beliefBase, possibility));
            allRules.addAll(findRules(beliefBase, negatedPossibility));

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

        if (rangeIterator == null) {
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

        while (iter != null && iter.hasNext()) {
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

//        logger.info("Expanded Rule " + ruleHead.toString() + " -> " + expandedLiterals);
        return expandedLiterals;
    }


    /**
     * An old generation algorithm kept for reference
     *
     * @deprecated An old generation algorithm kept for reference.
     */
    @Override
    @Deprecated
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
    @Deprecated
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
