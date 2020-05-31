package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import epistemic.reasoner.stub.StubReasonerSDK;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.spy;

/**
 * Creates a spy EpistemicDistribution and spy StubReasonerSDK object to
 * prevent actual reasoner requests.
 */
public class StubEpistemicDistributionBuilder extends EpistemicDistributionBuilder {
    private final StubReasonerSDK reasonerSDK;

    public StubEpistemicDistributionBuilder() {
        reasonerSDK = spy(new StubReasonerSDK());
    }

    public StubReasonerSDK getReasonerSDKSpy() {
        return reasonerSDK;
    }

    @Override
    public @NotNull StubEpistemicDistribution createDistribution(EpistemicAgent agent) {

        // Create a clone using the original builder
        // but make sure we set the reasoner SDK since we should mock that during testing.
        var distribution = super.createDistribution(agent);
        return spy(new StubEpistemicDistribution(agent, distribution.getManagedWorlds(), reasonerSDK));
    }

}
