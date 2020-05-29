package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import epistemic.reasoner.ReasonerSDK;
import epistemic.reasoner.stub.StubReasonerSDK;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.spy;

/**
 * Creates a spy EpistemicDistribution and defaults to a stubbed ReasonerSDK object to
 * prevent actual reasoner requests.
 */
public class StubEpistemicDistributionBuilder extends EpistemicDistributionBuilder {
    private final ReasonerSDK reasonerSDK;

    public StubEpistemicDistributionBuilder(ReasonerSDK reasonerSDK)
    {
        this.reasonerSDK = reasonerSDK;
    }

    public StubEpistemicDistributionBuilder() {
        this(new StubReasonerSDK());
    }

    @Override
    public @NotNull EpistemicDistribution createDistribution(EpistemicAgent agent) {

        // Create a clone using the original builder
        // but make sure we set the reasoner SDK since we should mock that during testing.
        var distribution = super.createDistribution(agent);
        return spy(new EpistemicDistribution(agent, distribution.getManagedWorlds(), reasonerSDK));
    }

}
