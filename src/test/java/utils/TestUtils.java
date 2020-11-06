package utils;

import epistemic.distribution.propositions.SingleValueProposition;
import epistemic.agent.stub.FixtureEpistemicDistributionBuilder;
import epistemic.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import epistemic.wrappers.WrappedTerm;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import utils.converters.EpistemicFormulaConverter;
import utils.converters.LiteralConverter;
import utils.converters.WrappedLiteralConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class TestUtils {

    // Fixture of EpistemicDistribution enumerations
    public static final FixtureEpistemicDistributionBuilder DEFAULT_DISTRIBUTION_FIXTURE = FixtureEpistemicDistributionBuilder.ofEntries(
            new AbstractMap.SimpleEntry<>("hand('Alice', Card)",
                    List.of("hand('Alice', 'AA')", "hand('Alice', '88')", "hand('Alice', 'A8')")),

            new AbstractMap.SimpleEntry<>("hand('Bob', Card)",
                    List.of("hand('Bob', 'AA')", "hand('Bob', '88')", "hand('Bob', 'A8')"))
    );
    public static final String[] ALL_EPISTEMIC_TEMPLATES = {
            "know(Formula)",
            "know(~Formula)",
            "~know(Formula)",
            "~know(~Formula)",

            "know(know(Formula))",
            "know(know(~Formula))",
            "know(~know(Formula))",
            "know(~know(~Formula))",

            "know(know(Formula))",
            "know(know(~Formula))",
            "know(~know(Formula))",
            "know(~know(~Formula))",
            "~know(know(Formula))",
            "~know(know(~Formula))",
            "~know(~know(Formula))",
            "~know(~know(~Formula))",

            "know(possible(~Formula))",
            "know(possible(Formula))",
            "possible(~know(~Formula))",
            "possible(know(~Formula))",
            "possible(~possible(~Formula))",
            "~possible(~possible(~Formula))",
    };

    public static final VarTerm FORMULA_VARTERM = ASSyntax.createVar("Formula");

    public static Map<WrappedLiteral, LinkedList<Literal>> createHandEnumeration(String agent, String... values) {
        var map = new HashMap<WrappedLiteral, LinkedList<Literal>>();
        var key = createHandWithVariable(agent);
        var valueList = new LinkedList<Literal>();

        for (String val : values) {
            valueList.add(createHandWithValue(agent, val).getCleanedLiteral());
        }

        map.put(key, valueList);
        return map;
    }

    public static Map<WrappedLiteral, SingleValueProposition> createHandEntry(String agent, String value) {

        var map = new HashMap<WrappedLiteral, SingleValueProposition>();

        var key = createHandWithVariable(agent);
        var val = createHandWithValue(agent, value);

        map.put(key, new SingleValueProposition(key, val));

        return map;
    }

    public static List<Trigger> toTriggerList(String... triggers) {
        List<Trigger> triggerList = new ArrayList<>();
        for(var trigger : triggers) {
            try {
                triggerList.add(ASSyntax.parseTrigger(trigger));
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse trigger: ", e);
            }
        }
        return triggerList;

    }

    public static WrappedLiteral createHandWithValue(String termOne, String termTwo) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createString(termTwo)));
    }

    public static WrappedLiteral createHandWithVariable(String termOne, String varName) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar(varName)));
    }

    public static WrappedLiteral createHandWithVariable(String termOne) {
        return new WrappedLiteral(ASSyntax.createLiteral("hand", ASSyntax.createString(termOne), ASSyntax.createVar()));
    }

    /**
     * Utility to flatten variable length arguments into a stream of single arguments.
     * Allows us to re-use fixtures.
     * <br>
     * <br>
     * Example: A stream with the arguments:
     * <br>
     * { (1, 2), (3, 4), (5, 6) }
     * <br>
     * Will be flattened to:
     * <br>
     * { (1), (2), (3), (4), (5), (6) }
     *
     * @param validFixture The variable length argument stream.
     * @return A Flattened arguments stream containing all arguments in the original stream.
     */
    public static Stream<Arguments> flattenArguments(@NotNull Stream<Arguments> validFixture) {
        return validFixture.flatMap((arguments) -> Arrays.stream(arguments.get()))
                .map(Arguments::of);
    }

    /**
     * Creates a WrappedLiteral object, suppressing any parse exception with a NullPointer (to reduce try/catch littering)
     *
     * @param source
     * @return
     */
    public static WrappedLiteral createWrappedLiteral(Object source) {
        WrappedLiteralConverter converter = new WrappedLiteralConverter();
        return (WrappedLiteral) converter.convert(source, null);
    }

    public static <R> List<R> aggregateLists(List<R> subscribed, List<R> other) {
        List<R> list = new ArrayList<>();
        list.addAll(subscribed);
        list.addAll(other);
        return list;
    }

    public static Stream<Arguments> transformLiteralArguments(Stream<Arguments> argumentsStream, Function<List<Literal>, List<Literal>> function) {
        LiteralConverter converter = new LiteralConverter();

        return argumentsStream.map(arguments -> {
            List<Literal> literals = new ArrayList<>();

            for (var arg : arguments.get()) {
                Object converted = converter.convert(arg, null);

                if (converted == null) {
                    literals.add(null);
                    continue;
                }

                if (!(converted instanceof Literal))
                    throw new RuntimeException("Failed to convert " + arg + " to literal");

                literals.add((Literal) converted);
            }

            return Arguments.of(function.apply(literals).toArray());
        });
    }

    public static Literal createLiteral(Object source) {
        LiteralConverter converter = new LiteralConverter();
        return (Literal) converter.convert(source, null);
    }

    public static EpistemicFormula createFormula(Object source) {
        EpistemicFormulaConverter converter = new EpistemicFormulaConverter();
        return (EpistemicFormula) converter.convert(source, null);
    }

    /**
     * Creates a list of literals from an Object array. Uses converter class
     * {@link LiteralConverter}. Accepts Strings and literals as object types.
     * @param literals The array of literals (can be of type string/literal).
     * @return A list of parsed literals.
     */
    public static List<Literal> toLiteralList(Object... literals) {
        List<Literal> list = new ArrayList<>();

        if(literals == null)
            return list;

        for (Object rawBelief : literals) {
            list.add(createLiteral(rawBelief));
        }

        return list;
    }

    /**
     * Uses a list of managed enumeration literals and creates a set of formulas
     * based on the formulaTemplates that are passed in. This function unifies the managed literals
     * with the variable term termToBind.
     *
     * @param allEnumerations All enumeration values
     * @param termToBind The term to bind the enumeration values with in the templates.
     * @param formulaTemplate The Formula template. Each template is parsed as a literal and unified.
     * @return A set of epistemic formulas built from the templates..
     */
    public static Set<EpistemicFormula> createFormulaMap(List<Literal> allEnumerations, VarTerm termToBind, String... formulaTemplate) {
        List<Literal> formulaTemplateLiteral = toLiteralList(formulaTemplate);
        Set<EpistemicFormula> resolvedFormulas = new HashSet<>();

        Unifier unifier = new Unifier();

        for (var litEnum : allEnumerations) {
            // Bind the variable functor to the enumeration value
            unifier.bind(termToBind, litEnum);

            for (var template : formulaTemplateLiteral) {
                var unifiedLiteral = (Literal) template.capply(unifier);

                if(!EpistemicFormula.isEpistemicLiteral(unifiedLiteral))
                    throw new IllegalArgumentException("Literal is not an epistemic formula: " + unifiedLiteral);

                resolvedFormulas.add(EpistemicFormula.fromLiteral(unifiedLiteral));
            }
        }


        return resolvedFormulas;
    }

    /**
     * Uses a list of managed enumeration literals and creates a set of formulas
     * based on the formulaTemplates that are passed in. This function unifies the managed literal
     * with the variable term 'Formula'.
     *
     * @param allEnumerations All enumeration values
     * @param formulaTemplate The Formula template. Each template is parsed as a literal and unified.
     * @return A set of epistemic formulas built from the templates..
     */
    public static Set<EpistemicFormula> createFormulaMap(List<Literal> allEnumerations, String... formulaTemplate) {
        return createFormulaMap(allEnumerations, FORMULA_VARTERM, formulaTemplate);
    }

    public static Set<EpistemicFormula> toFormulaSet(Object[] formulas) {
        Set<EpistemicFormula> set = new HashSet<>();

        if(formulas == null)
            return set;

        for (Object rawFormula : formulas) {
            set.add(createFormula(rawFormula));
        }

        return set;
    }

    public static <R> void assertIteratorEquals(Iterator<R> expected, Iterator<R> actual)
    {
        assertIteratorContains(expected, actual);

        // If there are no more elements in expected, the two iterators are equal.
        assertFalse(expected.hasNext(), "expected should not have any more elements");
    }


    /**
     * Creates a list of literals from an Object array. Uses converter class
     * {@link LiteralConverter}. Accepts Strings and literals as object types.
     * @param literals The array of literals (can be of type string/literal).
     * @return A list of parsed literals.
     */
    public static List<WrappedLiteral> toWrappedLiteralList(Object... literals) {
        List<WrappedLiteral> list = new ArrayList<>();

        if(literals == null)
            return list;

        for (Object rawBelief : literals) {
            list.add(createWrappedLiteral(rawBelief));
        }

        return list;
    }

    /**
     * Checks if the subset iterator contains the items in full. (I.e. subset is a subset of full).
     * This will also pass if the two iterators are equal.
     * @param full
     * @param subset
     */
    public static <R> void assertIteratorContains(Iterator<R> full, Iterator<R> subset) {
        if(full == subset)
            return;

        assertNotNull(full, subset + " should be null");
        assertNotNull(subset, "actual should be null");

        while(subset.hasNext())
        {
            assertTrue(full.hasNext(), "actual has more elements than expected");
            assertEquals(full.next(), subset.next(), "expected and actual elements should be equal");
        }
    }

    /**
     * Asserts that all terms (and terms in terms) are wrapped with
     * a WrappedTerm object.
     * @param literal The literal to check.
     */
    public static void assertWrappedTerms(Literal literal)
    {
        if(literal.getArity() <= 0)
            return;

        for(Term t : literal.getTerms())
        {
            assertTrue(t instanceof WrappedTerm, "all terms must be wrapped. term " + t.toString() + " is not wrapped.");

            WrappedTerm wrapped = (WrappedTerm) t;

            if(wrapped.getOriginalTerm() instanceof Literal)
                assertWrappedTerms((Literal) wrapped.getOriginalTerm());

        }
    }

    /**
     * Asserts that all terms (and terms in terms) are normalized.
     * @param literal The CLEANED literal.
     */
    public static void assertNormalizedTerms(Literal literal)
    {
        if(literal.getArity() <= 0)
            return;

        for(Term t : literal.getTerms())
        {
            if(t instanceof WrappedTerm)
                t = ((WrappedTerm) t).getOriginalTerm();

            if(!(t instanceof Literal) || t.isVar())
                continue;

            Literal termLit = (Literal) t;

            var wrapped = new WrappedLiteral(termLit);

            assertTrue(wrapped.isNormalized(), "all terms must be normalized. term " + t.toString() + " is not normalized.");

            assertNormalizedTerms(termLit);
        }
    }


    /**
     * Create all possible possible epistemic formulas up to 2 levels
     * (i.e. from know(formula) to know(know(formula))). Includes know, possible,
     * and all possible negation values.
     *
     * @param allEnumerations The list of managed enumeration values.
     * @return A set of epistemic formulas
     */
    public static Set<EpistemicFormula> createFormulaMap(List<Literal> allEnumerations) {
        return createFormulaMap(allEnumerations, ALL_EPISTEMIC_TEMPLATES);
    }



    public static Plan addBeliefPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.belief, triggerHead);
    }

    public static Plan delBeliefPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.belief, triggerHead);
    }

    public static Plan addAchievePlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.achieve, triggerHead);
    }

    public static Plan delAchievePlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.achieve, triggerHead);
    }

    public static Plan addTestPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.test, triggerHead);
    }

    public static Plan delTestPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.test, triggerHead);
    }

    public static Plan addPlan(Trigger.TEOperator op, Trigger.TEType type, String triggerHead) {
        try {
            Trigger te = new Trigger(op, type, ASSyntax.parseLiteral(triggerHead));
            return new Plan(null, te, null, null);
        } catch (ParseException e) {
            throw new AssertionError("failed to create belief plan for " + triggerHead + ". Message: " + e.getMessage());
        }
    }

}
