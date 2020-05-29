package epistemic.fixture;

import epistemic.EpistemicDistribution;
import epistemic.agent.stub.StubAgArch;
import epistemic.agent.stub.StubEpistemicAgent;
import epistemic.agent.stub.StubEpistemicDistributionBuilder;
import epistemic.formula.EpistemicFormula;
import epistemic.reasoner.stub.StubReasonerSDK;
import jason.JasonException;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * This class is used for setting up feature fixtures for testing the full AgArch.
 */
public class AgArchFixture {
    private final StubAgArch agArch;
    private final StubEpistemicAgent agent;
    private final EpistemicDistribution distribution;
    private final Set<EpistemicFormula> formulas;
    private final List<Literal> beliefs;

    protected AgArchFixture(StubEpistemicDistributionBuilder builder, Set<EpistemicFormula> formulas) {
        this(builder, List.of(), List.of(), formulas);
    }

    public AgArchFixture(StubEpistemicDistributionBuilder builder, @NotNull List<Literal> initialBeliefs, @NotNull List<Literal> beliefs, Set<EpistemicFormula> formulas) {
        agArch = spy(new StubAgArch(builder, false));
        this.agent = agArch.getAgSpy();

        // This relies on the agent not being loaded yet.
        this.agent.verifyNotLoaded();

        this.formulas = formulas;
        this.beliefs = beliefs;


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

        this.distribution = agent.getEpistemicDistribution();

    }

    public List<Literal> getBeliefs() {
        return beliefs;
    }

    public Set<EpistemicFormula> getFormulas() {
        return formulas;
    }

    public EpistemicDistribution getDistributionSpy() {
        return agArch.getAgSpy().getEpistemicDistributionSpy();
    }

    public StubAgArch getAgArchSpy() {
        return agArch;
    }

    public StubReasonerSDK getReasonerSDKSpy() {
        return agArch.getReasonerSDKSpy();
    }
}
