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
        var allRules = processLiterals(getEpistemicAgent().getBB(), this::possiblyFilter);
        allRules.addAll( processLiterals(getEpistemicAgent().getBB(), this::necessaryFilter));

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

            if(possiblyFilter(nextRule))
                processorChains.add(new PossiblyWorld(getEpistemicAgent(), nextRule, worldLiteralMatchers));
            else if (necessaryFilter(nextRule))
                processorChains.add(new NecessaryWorld(getEpistemicAgent(), nextRule, worldLiteralMatchers));

            for (var dependent : dependeeKeyLiterals.get(nextKey)) {
                var dependeeList = dependentKeyLiterals.get(dependent);
                dependeeList.remove(nextKey);
                if (dependeeList.isEmpty())
                    topQueue.add(dependent);
            }

        }


        ManagedWorlds managedWorlds = new ManagedWorlds(getEpistemicAgent());

        if(processorChains.size() != dependeeKeyLiterals.size())
        {
            logger.warning("There was a mismatch in rule processing.. could not process some rules");
        }

        // If there is a processor, we need to introduce a blank world for it to expand on
        if(!processorChains.isEmpty())
            managedWorlds.add(new World());

        while (!processorChains.isEmpty())
            managedWorlds = processorChains.poll().processManagedWorlds(managedWorlds);

        return managedWorlds;

    }

    @Override
    protected List<Literal> processLiterals(Iterable<Literal> literals) {
        return new ArrayList<>();
    }

    @Override
    protected ManagedWorlds generateWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap) {
        return null;
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
}
