package epistemic.agent.mock;

import epistemic.EpistemicDistribution;
import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import epistemic.reasoner.ReasonerSDK;
import epistemic.reasoner.mock.MockReasonerSDK;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.spy;

public class MockEpistemicDistributionBuilder extends EpistemicDistributionBuilder {
    private final ReasonerSDK reasonerSDK;
    private EpistemicDistribution epistemicDistribution;

    public MockEpistemicDistributionBuilder(ReasonerSDK reasonerSDK)
    {
        this.reasonerSDK = reasonerSDK;
    }

    public MockEpistemicDistributionBuilder() {
        this(new MockReasonerSDK());
    }

    @Override
    public @NotNull EpistemicDistribution createDistribution(EpistemicAgent agent) {

        // Create a clone using the original builder
        // but make sure we set the reasoner SDK since we should mock that during testing.
        var distribution = super.createDistribution(agent);
        var distClone = new EpistemicDistribution(agent, distribution.getManagedWorlds(), reasonerSDK);

        epistemicDistribution = spy(distClone);
        return epistemicDistribution;
    }

    public EpistemicDistribution getEpistemicDistribution() {
        return epistemicDistribution;
    }
}
