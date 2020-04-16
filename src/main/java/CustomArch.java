import eis.bb.KBeliefBase;
import epi.World;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.*;
import java.util.stream.Collectors;

public class CustomArch extends AgArch {

    private static final jason.asSyntax.Atom KB = ASSyntax.createAtom("kb");
    private static final Atom PROP_ANNOT = ASSyntax.createAtom("prop");

    Rule isPossibleRule = null;
    Rule distributionRule = null;

    @Override
    public void init() throws Exception {
        super.init();
        processDistribution();
    }

    /**
     * Process the distribution of worlds
     */
    private void processDistribution()
    {
        // Gets all literals in the kb belief base that are marked with 'prop'
        var propositionalLiterals = getPropLiterals();

        // Generate the map of literal enumerations
        var literalMap = generateLiteralEnumerations(propositionalLiterals);

        // Create the distribution of worlds
        List<World> worlds = generateWorlds(literalMap);

        System.out.println(worlds);
    }

    /**
     * Iterates through the belief base to find literals that match the form: kb::___(__,...)[prop]
     * @return A List of literals annotated with the prop annotation
     */
    private List<Literal> getPropLiterals()
    {
        // Get the agent's belief base
        var beliefBase = this.getTS().getAg().getBB();

        // Create a list of literals (rules or beliefs) marked with the [prop] annotation
        List<Literal> propositionalLiterals = new LinkedList<>();

        // We need to iterate all beliefs.
        // We can't use beliefBase.getCandidateBeliefs(...) [aka the pattern matching function] because
        // the pattern matching function doesn't allow us to pattern match by just namespace and annotation
        // (it requires a functor and arity)

        // Iterate through the belief base and find all literals in
        // the kb:: namespace that have the [prop] annotation
        for (Literal belief : beliefBase)
            if (belief.getNS().equals(KB) && belief.hasAnnot(PROP_ANNOT))
                propositionalLiterals.add(belief);

        return propositionalLiterals;
    }


    /**
     * Expands a rule (with variables) into a list of grounded literals. This essentially provides an enumeration of all variables values.
     * This maintains the functor and arity of the rule head, replacing variables with a value.
     *
     * For example, given the beliefs: [test("abc"), test("123")],
     * the rule original_rule(Test) :- test(Test) will be expanded to the following grounded literals:
     * [original_rule("abc"), original_rule("123")]
     *
     * @param rule The rule to expand.
     * @return A List of ground literals.
     */
    private LinkedList<Literal> expandRule(Rule rule)
    {
        // Obtain the head and body of the rule
        Literal ruleHead = rule.getHead();
        LogicalFormula ruleBody = rule.getBody();

        // Get all unifications for the rule body
        Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(this.getTS().getAg(), new Unifier());

        // Set up a list of expanded literals
        LinkedList<Literal> expandedLiterals = new LinkedList<>();

        // Unify each possible unification with the plan head and add it to the belief base.
        while (unifIterator.hasNext()) {
            Unifier unif = unifIterator.next();

            // Clone and apply the unification to the rule head
            Literal expandedRule = (Literal) ruleHead.capply(unif);
            System.out.println("Unifying " + rule.getFunctor() + " with " + unif + ". Result: " + expandedRule);

            // All unified/expanded rules should be ground.
            if(!expandedRule.isGround())
                System.out.println("The expanded rule (" + expandedRule + ") is not ground.");

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
    private Map<Literal, LinkedList<Literal>> generateLiteralEnumerations(List<Literal> propLiterals)
    {
        Map<Literal, LinkedList<Literal>> literalMap = new HashMap<>();

        for(Literal lit : propLiterals)
        {
            // Right now, we are only handling rules, but we can eventually extend support for beliefs
            if(lit.isRule())
            {
                // Expand the rule into possible enumerations
                LinkedList<Literal> expandedLiterals = expandRule((Rule) lit);

                // Put the enumerations into the mapping, with the original rule as the key
                literalMap.put(lit, expandedLiterals);
            }
        }

        return literalMap;
    }


    /**
     * Generate worlds given a mapping of all propositions. This essentially generates all permutations of each of the possible enumeration values.
     * @param allPropositionsMap This is a mapping of all literals (which are used to create the propositions used in each of the worlds)
     * @return A List of Possible worlds
     */
    private List<World> generateWorlds(Map<Literal, LinkedList<Literal>> allPropositionsMap) {

        // Create a blank world. Add it to a list.
        World firstWorld = new World();
        List<World> allWorlds = new ArrayList<>();
        allWorlds.add(firstWorld);


        // Go through each key in the map (aka all literals that go into each world):
        //    For all worlds in the list:
        //      If the world does not contain the predicate indicator, then
        //        Clone the world for each possible value in map for key.
        //      Add each value to their own separate worlds
        //      Add each cloned world to the list.

        // Iterate all "predicates". Each world should have one of the enumeration values from each key.
        for(Map.Entry<Literal, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet())
        {
            Literal curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            // Iterate list of current worlds
            ListIterator<World> worldIterator = allWorlds.listIterator();

            while(worldIterator.hasNext())
            {
                World world = worldIterator.next();

                // Add one possible enumeration value to the world,
                // cloning the world if there already exists a value for the current key.
                for(Literal val : allLiteralValues) {
                    World nextWorld = world;

                    // Clone the world if we already have a value in this world.
                    if (world.containsKey(curIndicator)) {
                        nextWorld = world.clone();
                    }

                    nextWorld.putLiteral(curIndicator, val);

                    if(!allWorlds.contains(nextWorld))
                        worldIterator.add(nextWorld);

                }
            }
        }

        // Only keep the worlds that are possible.
        return allWorlds.stream().filter(this::isPossibleWorld).collect(Collectors.toList());
    }

    private Rule getRule()
    {
        return (Rule) this.getTS().getAg().getBB().getCandidateBeliefs(new PredicateIndicator(new Atom("kb"), "is_possible", 3)).next();
    }

    /**
     * Uses the is_possible rule to determine if a world is possible.
     * This essentially injects any variable values into the rule's terms.
     *
     * @param nextWorld The world to check.
     * @return True if the world is possible, false otherwise.
     */
    private boolean isPossibleWorld(World nextWorld) {
        // Get the is_possible rule
        var isPossible = getRule();

        // Create a unifier
        Unifier unifier = new Unifier();

        // For each of the terms in the rule (i.e. one term would be 'kb::hand("Alice", Hand)'),
        // we want to see if one of the propositions in the world can unify any variables in that term (i.e. Hand).
        // If so, that variable is unified. We continue until all terms are unified. The unified values
        // are stored in the unifier object.
        for(Term t : isPossible.getTerms()) {
            for(Literal lit : nextWorld.values())
                if(unifier.unifies(t, lit))
                    break;
        }

        // We apply the values in the unifier to the rule.
        var isPossibleUnified = (Rule) isPossible.capply(unifier);

        // If there are any un-ground terms in the rule, that means the world does not satisfy the term variables and is therefore not a possible world.
        if(!isPossibleUnified.getHead().isGround() || !isPossibleUnified.getBody().isGround())
            return false;

        // The unified rule is executed to check if the world is possible. If hasNext returns true, then the rule was executed correctly.
        return isPossibleUnified.logicalConsequence(getTS().getAg(), unifier).hasNext();
    }


    private Map<PredicateIndicator, LinkedList<Literal>> getPropositionMap(List<Literal> originalLiterals)
    {
        Map<PredicateIndicator, LinkedList<Literal>> propositionMap = new HashMap<>();
        for(Literal literal : originalLiterals)
        {
            propositionMap.compute(literal.getPredicateIndicator(), (key, val) ->{
                if(val == null)
                    val = new LinkedList<>();

                val.add(literal.copy());
                return val;
            });
        }


        return propositionMap;
    }

    /**
     * This function tests world generation.
     * Further testing:
     * - All atoms
     * - All literals with vars
     * - Literals with anon vars?
     * - Rules as literals?
     * - Literals and atoms
     * - Other terms? (will we ever have Strings? etc.).
     */
    @Deprecated
    private void testWorldGenerationOld() {

        // 1. Get all propositions (expanding anything that is a rule).
        // Propositions will include atoms and literals.
        // i.e. alice, alice("AA"), etc.
        // For now, we will just worry about simple literals (i.e. alice(AA), alice("test", AA))
        // 2. Generate enumerations of all propositions:
        // Enumeration for Atoms (i.e. alice) should be alice, not(alice), (and ~alice???).
        // All propositions should be ground.
        var handAliceAA = ASSyntax.createLiteral("hand", ASSyntax.createString("Alice"), ASSyntax.createString("AA"));
        var handAlice88 = ASSyntax.createLiteral("hand", ASSyntax.createString("Alice"), ASSyntax.createString("88"));
        var handAliceA8 = ASSyntax.createLiteral("hand", ASSyntax.createString("Alice"), ASSyntax.createString("A8"));

        var handBobAA = ASSyntax.createLiteral("hand", ASSyntax.createString("Bob"), ASSyntax.createString("AA"));
        var handBob88 = ASSyntax.createLiteral("hand", ASSyntax.createString("Bob"), ASSyntax.createString("88"));
        var handBobA8 = ASSyntax.createLiteral("hand", ASSyntax.createString("Bob"), ASSyntax.createString("A8"));

        var handCharlieAA = ASSyntax.createLiteral("hand", ASSyntax.createString("Charlie"), ASSyntax.createString("AA"));
        var handCharlie88 = ASSyntax.createLiteral("hand", ASSyntax.createString("Charlie"), ASSyntax.createString("88"));
        var handCharlieA8 = ASSyntax.createLiteral("hand", ASSyntax.createString("Charlie"), ASSyntax.createString("A8"));

        var predList = new ArrayList<Literal>();
        predList.add(handAliceAA);
        predList.add(handAlice88);
        predList.add(handAliceA8);
        predList.add(handBobAA);
        predList.add(handBob88);
        predList.add(handBobA8);
        predList.add(handCharlieAA);
        predList.add(handCharlie88);
        predList.add(handCharlieA8);

        // 3. Generate mapping of possible enumerations for each proposition/predicate
        Map<PredicateIndicator, LinkedList<Literal>> allPropositionsMap = getPropositionMap(predList);

        // 4. Create a blank world. Add it to a list.
        World firstWorld = new World();
        List<World> allWorlds = new ArrayList<>();
        allWorlds.add(firstWorld);


        // 5. Go through each key in the map:
        //      For all worlds in the list:
        //      If the world does not contain the predicate indicator, then
        //      Clone the world for each possible value in map for key.
        //      Add each value to their own separate worlds
        //      Add each cloned world to the list.
        for(Map.Entry<PredicateIndicator, LinkedList<Literal>> predEntry : allPropositionsMap.entrySet())
        {
            PredicateIndicator curIndicator = predEntry.getKey();
            LinkedList<Literal> allLiteralValues = predEntry.getValue();

            ListIterator<World> worldIterator = allWorlds.listIterator();
            while(worldIterator.hasNext())
            {
                World world = worldIterator.next();

                for(Literal val : allLiteralValues) {
                    World nextWorld = world;

                    // Clone the world if we already have a value in this world.
                    if (world.containsPredicate(curIndicator)) {
                        nextWorld = world.clone();
                    }

                 //   nextWorld.putLiteral(val);

                    if(!allWorlds.contains(nextWorld))
                        worldIterator.add(nextWorld);

                }
            }
        }



        // Create fake atoms
        LinkedList<Literal> list = new LinkedList<>();

        list.add(ASSyntax.createLiteral(new Atom("kb"), "alice_card", ASSyntax.createVar("Card")));

        list.add(ASSyntax.createAtom("bob"));
        list.add(ASSyntax.createAtom("charlie"));

        LogicalFormula formula = createWorldFormula(list);
        LogExpr logExpr = (LogExpr) formula.cloneNS(new Atom("kb"));

        if(logExpr == null)
            throw new RuntimeException("Failed to generate formula.");

        Iterator<Unifier> unifierIterator = logExpr.logicalConsequence(this.getTS().getAg(), new Unifier());




        logExpr.toString();

    }


    @Deprecated
    private void processDistributionRule()
    {
        if (distributionRule != null) {


            LogicalFormula ruleBody = distributionRule.getBody();
            Literal planHead = distributionRule.getHead();

            // Get all unifications for the rule body
            Iterator<Unifier> unifIterator = ruleBody.logicalConsequence(this.getTS().getAg(), new Unifier());

            // Unify each possible unification with the plan head and add it to the belief base.
            while (unifIterator.hasNext()) {
                Unifier unif = unifIterator.next();

                if (isPossibleRule != null) {
                    Iterator<Unifier> isPossibleUnif = isPossibleRule.logicalConsequence(this.getTS().getAg(), unif);
                    if (!isPossibleUnif.hasNext()) {
                        System.out.println("Unification " + unif.toString() + " is not possible. Skipping.");
                        continue;
                    }
                }
                Literal expandedRule = (Literal) planHead.capply(unif);
                System.out.println("Unifying " + distributionRule.getFunctor() + " with " + unif + ". Result: " + expandedRule);

                this.getTS().getAg().getBB().add(expandedRule);
                this.getTS().getAg().getBB().remove(distributionRule);
            }
        }
    }

    @Deprecated
    private List<String> generateStaticProps(LogicalFormula logicalFormula)
    {
        // Is a map better? i.e. prop name -> val
        List<String> worlds = new ArrayList<>();



        return worlds;
    }

    @Deprecated
    private Rule createWorldRule(List<Literal> worldProps)
    {
        LinkedList<Literal> linkedWorldProps = new LinkedList<>();
        if(!(worldProps instanceof LinkedList))
            Collections.copy(linkedWorldProps, worldProps);
        else
            linkedWorldProps = (LinkedList<Literal>) worldProps;

        return new Rule(ASSyntax.createAtom("world"), createWorldFormula(linkedWorldProps));
    }

    @Deprecated
    private LogicalFormula createWorldFormula(LinkedList<Literal> worldProps)
    {
        if(worldProps.isEmpty())
            return null;

        if(worldProps.size() == 1)
            return new LogExpr(LogExpr.LogicalOp.none, worldProps.get(0));

        var curLiteral = worldProps.removeFirst();
        var nextFormula = createWorldFormula(worldProps);

        return new LogExpr(curLiteral, LogExpr.LogicalOp.and, nextFormula);
    }

}
