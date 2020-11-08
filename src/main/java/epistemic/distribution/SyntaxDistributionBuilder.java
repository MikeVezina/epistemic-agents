package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.World;
import epistemic.distribution.generator.CallbackLogicalConsequence;
import epistemic.distribution.generator.NecessaryGenerator;
import epistemic.distribution.generator.PossiblyGenerator;
import epistemic.distribution.generator.WorldGenerator;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
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
    private boolean hasAnnotation(String annotation, Literal literal) {
        return literal.getAnnot(annotation) != null;
    }

    @Override
    protected String acceptsLiteral(Literal literal) {
        // Currently only supports rules (no beliefs...)
        // We do not accept negated rules (doesn't make sense semantically)
        if(!literal.isRule() || literal.negated() || !literal.hasAnnot())
            return null;

        if(hasAnnotation(POSSIBLY_ANNOT, literal))
            return POSSIBLY_ANNOT;

        if(hasAnnotation(NECESSARY_ANNOT, literal))
            return NECESSARY_ANNOT;

        return null;
    }

    @Override
    protected ManagedWorlds generateWorlds(Map<String, List<Literal>> allPropositionsMap) {
        // Get rule literals
        var allRules = new ArrayList<Literal>();

        // Merge all value lists to single list
        for(var val : allPropositionsMap.values())
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
        if (!processorChains.isEmpty())
            managedWorlds.add(new World());

        while (!processorChains.isEmpty())
            managedWorlds = processorChains.poll().processManagedWorlds(managedWorlds);

        return managedWorlds;
    }

    /**
     * Generates the topology for rules.
     *
     * @param allRules The list of all relevant rules for world generation.
     * @param originalRuleMap The output map for mapping the wrapped rule head to the original rule
     * @param dependentGroundLiterals The output map for rule dependencies on GROUND wrapped literals
     * @param dependentKeyLiterals The output map for rule head wrapped literal dependencies on other rule heads (i.e. which rules depend on what)
     * @param dependeeKeyLiterals The output map for the inverse of the dependentKeyLiterals output
     */
    private void createRuleTopology(ArrayList<Literal> allRules, Map<WrappedLiteral, Rule> originalRuleMap, Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals) {
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
                    if (key.canUnify(dep)) {
                        // Entry.key is dependent on  Key
                        dependentKeyLiterals.get(dependentKey).add(key);
                        dependeeKeyLiterals.get(key).add(dependentKey);
                    }
                }
            }
        }
    }

    @NotNull
    private Queue<WorldGenerator> getOrderedWorldGenerators(Map<WrappedLiteral, Rule> originalRuleMap, Map<WrappedLiteral, Set<WrappedLiteral>> dependentGroundLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependentKeyLiterals, Map<WrappedLiteral, Set<WrappedLiteral>> dependeeKeyLiterals) {
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

            if (hasAnnotation(POSSIBLY_ANNOT, nextRule))
                processorChains.add(new PossiblyGenerator(getEpistemicAgent(), nextRule, worldLiteralMatchers));
            else if (hasAnnotation(NECESSARY_ANNOT, nextRule))
                processorChains.add(new NecessaryGenerator(getEpistemicAgent(), nextRule, worldLiteralMatchers));

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

    /**
     * Hooks into the logical consequence function and determines the literals that the rule evaluates.
     * Example: rule(X) :- lit & not other(X,2,3) returns a map {rule(X) -> {lit, other((X,2,3)}
     *
     * @param r The rule to get dependents for.
     * @return A Map entry for the rule and its dependents
     */
    private Map.Entry<WrappedLiteral, Set<WrappedLiteral>> getRuleDependents(Rule r) {
        Set<WrappedLiteral> literalList = new HashSet<>();

        r.getBody().logicalConsequence(new CallbackLogicalConsequence(getEpistemicAgent(), (l, u) -> {
            literalList.add(new WrappedLiteral(l));
            return null;
        }), new Unifier());

        return new AbstractMap.SimpleEntry<>(new WrappedLiteral(r.getHead()), literalList);
    }

}
