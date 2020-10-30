package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.*;

public class EpistemicWorldDistributionBuilder extends EpistemicDistributionBuilder {

    public static final Atom WORLD_ANNOT = ASSyntax.createAtom("world");
    public static final Atom APPEND_ANNOT = ASSyntax.createAtom("append");

    @Override
    protected List<Literal> processLiterals(Iterable<Literal> literals) {
        var listLiterals = processLiterals(literals, this::worldFilter);
        listLiterals.addAll(processLiterals(literals, this::extendFilter));

        return listLiterals;
    }

    private Boolean extendFilter(Literal literal) {
        return literal.hasAnnot(APPEND_ANNOT);
    }

    /**
     * Adds literals to propLiterals marked with the [prop] annotation. Does nothing otherwise.
     *
     * @param literal The literal
     */
    private boolean worldFilter(Literal literal) {
        return literal.hasAnnot(WORLD_ANNOT);
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

        // Create new worlds (the ones marked with '[world')
        List<World> allWorlds = createNewWorlds(allPropositionsMap);

        // Append annotations using extension rules
        extendWorlds(allPropositionsMap, allWorlds);

        // Only keep the worlds that are valid.
        return allWorlds.stream().collect(ManagedWorlds.WorldCollector(getEpistemicAgent()));
    }

    private void extendWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap, List<World> allWorlds) {
        // No worlds to extend
        if(allWorlds.isEmpty())
            return;

        // Extend all worlds ([append] annotations)
        for (Map.Entry<WrappedLiteral, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            if(!curIndicator.getOriginalLiteral().hasAnnot(APPEND_ANNOT))
                continue;

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

                for(World findWorld : allWorlds)
                {
                    Set<Proposition> props = matchAndExtendWorld(worldMatcher, findWorld);
                }

            }
        }

    }

    /**
     * Matches a world based on mutual proposition keys. If two worlds completely match, append all propositions
     * @param worldMatcher
     * @param findWorld
     * @return A set of propositions that need to be added to the world (or null if not matched)
     */
    private Set<Proposition> matchAndExtendWorld(World worldMatcher, World findWorld) {
        Set<Proposition> propsToAppend = new HashSet<>();

        for(Proposition prop : worldMatcher.valueSet()) {
            // If the two worlds contain the same keys and their values DON'T match, just return (don't append)
            if (findWorld.containsKey(prop.getKey())) {
                if (!findWorld.get(prop.getKey()).equals(prop))
                    return null;
            }
            else propsToAppend.add(prop);
        }

        for(Proposition prop : propsToAppend)
            findWorld.putProposition(prop);

        return propsToAppend;
    }

    private List<World> createNewWorlds(Map<WrappedLiteral, LinkedList<Literal>> allPropositionsMap) {
        List<World> allWorlds = new LinkedList<>();

        // Create all worlds ([world] annotations)
        for (Map.Entry<WrappedLiteral, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet()) {
            WrappedLiteral curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            if(!curIndicator.getOriginalLiteral().hasAnnot(WORLD_ANNOT))
                continue;

            for (var litValues : allLiteralValues) {
                // Separate the terms in the world indicator
                List<Term> terms = curIndicator.getCleanedLiteral().getTerms();

                World curWorld = new World();
                for (int i = 0, termsSize = terms.size(); i < termsSize; i++) {
                    Term ungroundTerm = terms.get(i);
                    Term groundTerm = litValues.getTerm(i);

                    if (!(ungroundTerm instanceof Literal) || !(groundTerm instanceof Literal)) {
                        System.out.println("Not literal: " + ungroundTerm + " or " + groundTerm);
                        continue;
                    }

                    WrappedLiteral keyTerm = new WrappedLiteral((Literal) ungroundTerm);
                    curWorld.putLiteral(keyTerm, (Literal) groundTerm);
                }

                allWorlds.add(curWorld);
            }
        }

        return allWorlds;
    }

}
