package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EpistemicWorldDistributionBuilder extends EpistemicDistributionBuilder {

    public static final String WORLD_ANNOT = "world";
    public static final String APPEND_ANNOT = "append";

    private final Logger metricsLogger = Logger.getLogger(getClass().getName() + " - Metrics");
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    protected List<Literal> processLiterals(Iterable<Literal> literals) {
        var listLiterals = processLiterals(literals, this::worldFilter);
        listLiterals.addAll(processLiterals(literals, this::extendFilter));

        return listLiterals;
    }

    private Boolean extendFilter(Literal literal) {
        return literal.getAnnot(APPEND_ANNOT) != null;
    }

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private boolean worldFilter(Literal literal) {
        return literal.getAnnot(WORLD_ANNOT) != null;
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
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(getEpistemicAgent(), new Unifier());

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

        Map<WrappedLiteral, List<Literal>> newWorldRules = allPropositionsMap.entrySet().stream().filter(e -> worldFilter(e.getKey().getOriginalLiteral())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<WrappedLiteral, List<Literal>> extendWorldRules = allPropositionsMap.entrySet().stream().filter(e -> extendFilter(e.getKey().getOriginalLiteral())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<World, List<World>> worldMatchers = createWorldMatchers(extendWorldRules);

        // Create new worlds (the ones marked with '[world')
        List<World> allWorlds = createNewWorlds(newWorldRules, worldMatchers);


        // Only keep the worlds that are valid.
        return allWorlds.stream().collect(ManagedWorlds.WorldCollector(getEpistemicAgent()));
    }

    /**
     * @param indicator
     * @param groundLiteral
     * @return The new World containing all ground literals, or null if
     */
    private World createWorldFromGroundLiteral(@NotNull WrappedLiteral indicator, @NotNull Literal groundLiteral, String annotationFunctor) {
        World newWorld = this.createWorldFromGroundLiteral(indicator, groundLiteral);

        Literal annotation = groundLiteral.getAnnot(annotationFunctor);

        // Check to see if world ID is provided
        if (annotation == null || !annotation.isLiteral() || !annotation.hasTerm()) {
            logger.warning("No ID term for literal annotation (" + annotationFunctor + ") in " + groundLiteral + "");
            throw new RuntimeException("No ID term for literal annotation (" + annotationFunctor + ") in " + groundLiteral + "");
        }

        Term worldId = annotation.getTerm(0);
        if (!worldId.isGround()) {
            logger.warning("Annotation for World ID is not ground..?");
            throw new RuntimeException("World annotation term not ground!");
        }

        newWorld.setId(worldId);

        return newWorld;
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

        for (int i = 0, termsSize = terms.size(); i < termsSize; i++) {
            Term ungroundTerm = terms.get(i);
            Term groundTerm = groundLiteral.getTerm(i);

            if (!(ungroundTerm instanceof Literal) || !(groundTerm instanceof Literal)) {
                System.err.println("Not literal: " + ungroundTerm + " or " + groundTerm);
                continue;
            }

            WrappedLiteral keyTerm = new WrappedLiteral((Literal) ungroundTerm);
            Literal groundValue = (Literal) groundTerm;

            // Insert proposition into world
            newWorld.putLiteral(keyTerm, groundValue);
        }

        return newWorld;
    }

    private Map<World, List<World>> createWorldMatchers(Map<WrappedLiteral, List<Literal>> extendWorldRules) {

        Map<World, List<World>> matchers = new HashMap<>();

        // Extend all worlds ([append] annotations)
        for (Map.Entry<WrappedLiteral, List<Literal>> predEntry : extendWorldRules.entrySet()) {
            var curIndicator = predEntry.getKey();
            var allLiteralValues = predEntry.getValue();

            // Go through all unification values for the rules (i.e. the literals to match/extend the worlds with)
            for (var groundValue : allLiteralValues) {
                var matchWorld = createWorldFromGroundLiteral(curIndicator, groundValue, APPEND_ANNOT);

                if(!matchers.containsKey(matchWorld))
                    matchers.put(matchWorld, new ArrayList<>());

                matchers.get(matchWorld).add(matchWorld);
            }

        }

        return matchers;
    }

    private void extendWorlds(Map<WrappedLiteral, LinkedList<Literal>> worldRulesMap, List<World> allWorlds) {
        // No worlds to extend
        if (allWorlds.isEmpty())
            return;


        // Maintain the extensions for each world until the end...
        Map<World, Set<Proposition>> extendedWorldProps = new HashMap<>();

        // Extend all worlds ([append] annotations)
        for (Map.Entry<WrappedLiteral, LinkedList<Literal>> predEntry : worldRulesMap.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            // Only grab the append rules
            if (extendFilter(curIndicator.getOriginalLiteral()))
                continue;

            // Go through all unification values for the rules (i.e. the literals to match/extend the worlds with)
            for (var litValues : allLiteralValues) {
                // Separate the terms in the world indicator
                List<Term> terms = curIndicator.getCleanedLiteral().getTerms();

                World worldMatcher = new World();
                for (int i = 0, termsSize = terms.size(); i < termsSize; i++) {
                    Term ungroundTerm = terms.get(i);
                    Term groundTerm = litValues.getTerm(i);

                    if (!(ungroundTerm instanceof Literal) || !(groundTerm instanceof Literal)) {
                        System.out.println("Not literal: " + ungroundTerm + " or " + groundTerm);
                        continue;
                    }

                    WrappedLiteral keyTerm = new WrappedLiteral((Literal) ungroundTerm);
                    worldMatcher.putLiteral(keyTerm, (Literal) groundTerm);
                }


            }
        }


    }

    /**
     * Matches a world based on mutual proposition keys. If two worlds completely match, append all propositions
     *
     * @param worldMatchers
     * @param findWorld
     * @return A set of propositions that were added to the world
     */
    private void matchAndExtendWorld(World findWorld, Map<World, List<World>> worldMatchers) {
        Map<World, Set<Proposition>> propsToAppend = new HashMap<>();

        // Because the IDs will match between worlds, we can find extension worlds using the mapping as follows:
        List<World> extendedWorlds = worldMatchers.get(findWorld);

//
//
//        for (var worldMatcher : worldMatchers.entrySet()) {
//            var matcherId = worldMatcher.getKey().getId();
//
//            // Skip if not matched (Matching values is quicker than props..)
//            if (!findWorld.getId().equals(matcherId))
//                continue;
//
//            if(!propsToAppend.containsKey(worldMatcher))
//                propsToAppend.put(worldMatcher, new HashSet<>());
//
//            // If a prop exists, check that all values match
//            propsToAppend.get(worldMatcher).addAll(worldMatcher.valueSet());
//        }

        // Finally add all new props to the world
        for (var world : extendedWorlds)
            for(var p : world.valueSet())
                findWorld.putProposition(p);

    }

    private List<World> createNewWorlds(Map<WrappedLiteral, List<Literal>> newWorldRules, Map<World, List<World>> worldExtenders) {
        List<World> allWorlds = new LinkedList<>();

        long totalExtendTime = 0;
        long totalCreationTime = System.nanoTime();

        // Create all worlds ([world] annotations)
        for (Map.Entry<WrappedLiteral, List<Literal>> predEntry : newWorldRules.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            List<Literal> allLiteralValues = predEntry.getValue();

            for (var litValues : allLiteralValues) {
                World curWorld = createWorldFromGroundLiteral(curIndicator, litValues, WORLD_ANNOT);

                // Extend new world with any matchers
                long startExtend = System.nanoTime();
                matchAndExtendWorld(curWorld, worldExtenders);
                totalExtendTime += (System.nanoTime() - startExtend);


                allWorlds.add(curWorld);
            }
        }

        totalCreationTime = System.nanoTime() - totalCreationTime;

        metricsLogger.info("Total Creation Time (ms): " + totalCreationTime / 1000000);
        metricsLogger.info("Total Extend Time (ms): " + totalExtendTime / 1000000);

        return allWorlds;
    }

}
