package epistemic.agent.stub;

import epistemic.ManagedWorlds;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.SyntaxDistributionBuilder;
import epistemic.reasoner.stub.StubReasonerSDK;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.spy;

/**
 * Creates a spy EpistemicDistribution and spy StubReasonerSDK object to
 * prevent actual reasoner requests.
 */
public class StubEpistemicDistributionBuilder extends SyntaxDistributionBuilder {
    private final StubReasonerSDK reasonerSDK;

    public StubEpistemicDistributionBuilder() {
        reasonerSDK = spy(new StubReasonerSDK());
    }

    public StubReasonerSDK getReasonerSDKSpy() {
        return reasonerSDK;
    }

    @Override
    public @NotNull StubEpistemicDistribution createDistribution(@NotNull EpistemicAgent agent) {

        // Create a clone using the original builder
        // but make sure we set the reasoner SDK since we should mock that during testing.
        var distribution = super.createDistribution(agent);
        return spy(new StubEpistemicDistribution(agent, distribution.getManagedWorlds(), reasonerSDK));
    }

    @Override
    protected ManagedWorlds generateWorlds(Map<String, List<Literal>> allPropositionsMap) {
        return null;
    }

}
