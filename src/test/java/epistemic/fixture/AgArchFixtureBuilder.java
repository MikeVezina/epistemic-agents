package epistemic.fixture;

import epistemic.agent.stub.FixtureEpistemicDistributionBuilder;
import epistemic.agent.stub.StubAgArch;
import epistemic.formula.EpistemicFormula;
import jason.JasonException;
import jason.asSyntax.Literal;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.spy;
import static utils.TestUtils.*;

public class AgArchFixtureBuilder {
    private final FixtureEpistemicDistributionBuilder distributionBuilder;
    private List<Literal> initialBeliefs;

    public AgArchFixtureBuilder(FixtureEpistemicDistributionBuilder distributionBuilder) {
        this.distributionBuilder = distributionBuilder;
        this.initialBeliefs = new ArrayList<>();
    }

    /**
     * Initial beliefs are beliefs added to the belief base before
     * the agent is initialized and loaded.
     *
     * @param beliefs The beliefs to initialize.
     * @return The builder object
     */
    public AgArchFixtureBuilder initialBeliefs(Object... beliefs) {
        initialBeliefs = toLiteralList(beliefs);
        return this;
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

    public Arguments buildArguments(List<Literal> queryBeliefs, Set<EpistemicFormula> formulas) {
        return Arguments.of(
                this.buildArchSpy(),
                queryBeliefs == null ? buildQueryBeliefs() : queryBeliefs,
                formulas == null ? buildFormulas() : formulas
        );
    }

    public Arguments buildArguments(List<Literal> queryBeliefs) {
        return Arguments.of(
                this.buildArchSpy(),
                queryBeliefs == null ? buildQueryBeliefs() : queryBeliefs
        );
    }

    public Arguments buildArguments(Set<EpistemicFormula> formulas) {
        return Arguments.of(
                this.buildArchSpy(),
                formulas == null ? buildFormulas() : formulas
        );
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
