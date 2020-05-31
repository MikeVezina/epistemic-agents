package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;
import org.jetbrains.annotations.Nullable;

import static org.mockito.Mockito.*;

public class StubEpistemicAgent extends EpistemicAgent {

    public StubEpistemicAgent(StubEpistemicDistributionBuilder mockEpistemicDistributionBuilder) {
        super(mockEpistemicDistributionBuilder);
    }

    public void verifyNotLoaded()
    {
        verify(this, times(0)).agentLoaded();
    }

    public void verifyLoaded()
    {
        verify(this, atLeastOnce()).agentLoaded();
    }

    @Override
    public @Nullable StubEpistemicDistribution getEpistemicDistribution() {
        return (StubEpistemicDistribution) super.getEpistemicDistribution();
    }

    /**
     * calls load while wrapping the exception in a runtime exception
     */
    public void suppressedLoad() {

        try {
            this.load("");
        } catch (JasonException e) {
            throw new RuntimeException("Failed to load agent: ", e);
        }
    }
}
