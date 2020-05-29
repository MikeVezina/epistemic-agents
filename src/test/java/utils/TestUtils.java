package utils;

import epistemic.Proposition;
import epistemic.agent.stub.FixtureEpistemicDistributionBuilder;
import epistemic.fixture.AgArchFixture;
import epistemic.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import utils.converters.EpistemicFormulaConverter;
import utils.converters.LiteralConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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
    private static final String FORMULA_VARTERM = "Formula";

    public static Map<WrappedLiteral, LinkedList<Literal>> createHandEnumeration(String agent, String... values) {
        var map = new HashMap<WrappedLiteral, LinkedList<Literal>>();
        var key = createHandWithVariable(agent);
        var valueList = new LinkedList<Literal>();

        for (String val : values) {
            valueList.add(createHandWithValue(agent, val).getOriginalLiteral());
        }

        map.put(key, valueList);
        return map;
    }

    public static Map<WrappedLiteral, Proposition> createHandEntry(String agent, String value) {

        var map = new HashMap<WrappedLiteral, Proposition>();

        var key = createHandWithVariable(agent);
        var val = createHandWithValue(agent, value);

        map.put(key, new Proposition(key, val));

        return map;
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
     * @param litString
     * @return
     */
    public static WrappedLiteral createWrappedLiteral(String litString) {
        try {
            return new WrappedLiteral(ASSyntax.parseLiteral(litString));
        } catch (ParseException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
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
    public static List<Literal> toLiteralList(Object[] literals) {
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
        Set<EpistemicFormula> templateFormula = toFormulaSet(formulaTemplate);
        Set<EpistemicFormula> resolvedFormulas = new HashSet<>();

        Unifier unifier = new Unifier();

        for (var litEnum : allEnumerations) {
            // Bind the variable functor to the enumeration value
            unifier.bind(ASSyntax.createVar("Formula"), litEnum);

            for (var template : templateFormula) {
                resolvedFormulas.add(template.capply(unifier));
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
        return createFormulaMap(allEnumerations, ASSyntax.createVar(FORMULA_VARTERM), formulaTemplate);
    }

    public static AgArchFixture createAgArchFixture() {
        return createAgArchFixture(List.of(), List.of());
    }

    public static AgArchFixture createAgArchFixture(List<String> queryBels) {
        return createAgArchFixture(List.of(), queryBels);
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

    /**
     * Use default distribution with a list of initial beliefs and beliefs to query.
     * @param initialBels
     * @param beliefsToQuery
     * @return
     */
    public static AgArchFixture createAgArchFixture(List<String> initialBels, List<String> beliefsToQuery) {

        // Create formulas. All enumerations will be
        var formulas = createFormulaMap(DEFAULT_DISTRIBUTION_FIXTURE.getValues());
        var initBelsLit = new ArrayList<Literal>();
        var belsToQuery = new ArrayList<Literal>();

        for(String initBel : initialBels)
            initBelsLit.add(createLiteral(initBel));

        for(String bel : beliefsToQuery)
            belsToQuery.add(createLiteral(bel));

        var fixture = new AgArchFixture(DEFAULT_DISTRIBUTION_FIXTURE,initBelsLit, belsToQuery, formulas);

        // Set reasoner to evaluate to true for the given formulas
        fixture.getReasonerSDKSpy().setFormulaValuation(formulas, true);

        return fixture;
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

}
