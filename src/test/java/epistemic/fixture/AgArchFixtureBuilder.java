package epistemic.fixture;

import epistemic.agent.stub.FixtureEpistemicDistributionBuilder;
import epistemic.agent.stub.StubAgArch;
import epistemic.formula.EpistemicFormula;
import jason.JasonException;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.spy;
import static utils.TestUtils.*;

/**
 * Allows for building a fully stubbed AgArch object, with
 * all necessary Epistemic components.
 * 
 * Initial beliefs can be provided.
 * 
 * Formula sets and belief query lists can also be constructed.
 * This builder also allows for building test Arguments directly, see {@link AgArchFixtureBuilder#buildArguments}
 *
 */
public class AgArchFixtureBuilder {
    private final FixtureEpistemicDistributionBuilder distributionBuilder;
    private List<Literal> initialBeliefs;

    public AgArchFixtureBuilder(FixtureEpistemicDistributionBuilder distributionBuilder, Object... initialBeliefs) {
        this.distributionBuilder = distributionBuilder;
        this.initialBeliefs = toLiteralList(initialBeliefs);
    }

    private AgArchFixtureBuilder(AgArchFixtureBuilder builder)
    {
        this(builder.distributionBuilder, builder.initialBeliefs.toArray());
    }

    /**
     * Initial beliefs are beliefs added to the belief base before
     * the agent is initialized and loaded.
     *
     * @param beliefs The beliefs to initialize.
     * @return A cloned builder object
     */
    public AgArchFixtureBuilder initialBeliefs(Object... beliefs) {
        var clone = new AgArchFixtureBuilder(this);
        clone.initialBeliefs = toLiteralList(beliefs);
        return clone;
    }

    public AgArchFixtureBuilder initialBeliefs(List<Object> beliefs) {
        return this.initialBeliefs(beliefs.toArray());
    }

    public List<Literal> buildQueryBeliefs(Object... beliefs) {
        return toLiteralList(beliefs);
    }

    public List<Literal> buildQueryBeliefs(List<Object> beliefs) {
        return this.buildQueryBeliefs(beliefs.toArray());
    }

    public Set<EpistemicFormula> buildFormulas(Object... beliefs) {
        return toFormulaSet(beliefs);
    }

    /**
     * @return A copy of the initial beliefs that can be used as query beliefs.
     */
    public List<Literal> buildQueryBeliefsFromInitial()
    {
        return List.copyOf(initialBeliefs);
    }

    /**
     * Injects the built AgArch as the first argument. Additional arguments will be forwarded to the returned Arguments
     * object starting from Index 1, i.e.: Arguments {AgArch, Object...}
     * @param additionalArguments The additional arguments to add
     * @return
     */
    public Arguments buildArguments(@NotNull Object... additionalArguments) {
        var args = new ArrayList<>(List.of(additionalArguments));
        args.add(0, this.buildArchSpy());
        return Arguments.of(args.toArray());
    }

    /**
     * Uses formula templates to create a set of epistemic formulas.
     * Uses the Variable term 'Formula' in the templates to substitute for
     * actual managed epistemic values.
     * <p>
     * The string templates will be parsed as literals and will unify
     * the 'Formula' VarTerm with all EpistemicDistribution literals.
     * <p>
     * Uses {@link utils.TestUtils#createFormulaMap(List, String...)}.
     *
     * @param formulaTemplates The string templates.
     * @return The builder.
     */
    public Set<EpistemicFormula> buildFormulas(String... formulaTemplates) {
        return createFormulaMap(distributionBuilder.getValues(), formulaTemplates);
    }

    public StubAgArch buildArchSpy() {
        var agArch = spy(new StubAgArch(distributionBuilder, false));
        var agent = agArch.getAgSpy();
        try {
            // Insert initial beliefs here.
            // This needs to happen before the agent is loaded
            for (var bel : initialBeliefs)
                agent.addInitialBel(bel);

            // Load the agent
            agent.load("");
        } catch (JasonException e) {
            throw new RuntimeException(e);
        }

        return agArch;
    }
}
